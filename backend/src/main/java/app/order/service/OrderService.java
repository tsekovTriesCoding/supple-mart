package app.order.service;

import app.cart.model.Cart;
import app.cart.service.CartService;
import app.cartitem.model.CartItem;
import app.config.CacheConfig;
import app.exception.BadRequestException;
import app.exception.ResourceNotFoundException;
import app.exception.UnauthorizedException;
import app.notification.event.OrderDeliveredEvent;
import app.notification.event.OrderPlacedEvent;
import app.notification.event.OrderShippedEvent;
import app.order.dto.CreateOrderRequest;
import app.order.dto.OrderResponse;
import app.order.dto.OrderStats;
import app.order.dto.OrdersResponse;
import app.order.mapper.OrderMapper;
import app.order.model.Order;
import app.order.model.OrderItem;
import app.order.model.OrderStatus;
import app.order.repository.OrderRepository;
import app.product.service.ProductService;
import app.user.model.User;
import app.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final CartService cartService;
    private final UserService userService;
    private final ProductService productService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public OrdersResponse getUserOrders(UUID userId, String statusStr, LocalDateTime startDate,
                                        LocalDateTime endDate, Integer page, Integer limit) {
        OrderStatus status = null;
        if (statusStr != null && !statusStr.isEmpty()) {
            try {
                status = OrderStatus.valueOf(statusStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid order status provided: '{}'. Ignoring status filter.", statusStr);
            }
        }

        int pageNumber = page != null ? page : 0;
        int pageSize = limit != null ? limit : 10;

        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        Page<Order> orderPage = orderRepository.findUserOrdersWithFilters(
                userId, status, startDate, endDate, pageable
        );

        return orderMapper.toOrdersResponse(orderPage);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(UUID orderId, UUID userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order with ID " + orderId + " not found"));

        if (!order.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You are not authorized to view this order");
        }

        return orderMapper.toOrderResponse(order);
    }

    /**
     * Creates a new order from the user's cart.
     * Evicts dashboard stats cache since order counts change.
     */
    @Transactional
    @CacheEvict(value = CacheConfig.DASHBOARD_STATS_CACHE, allEntries = true)
    public OrderResponse createOrder(UUID userId, CreateOrderRequest request) {
        User user = userService.getUserById(userId);

        Cart cart;
        try {
            cart = cartService.getCartWithItemsForOrder(userId);
        } catch (ResourceNotFoundException e) {
            // No cart exists for user - treat same as empty cart
            throw new BadRequestException("Cart is empty. Cannot create order with no items");
        }

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new BadRequestException("Cart is empty. Cannot create order with no items");
        }

        for (CartItem cartItem : cart.getItems()) {
            productService.reserveInventory(cartItem.getProduct().getId(), cartItem.getQuantity());
        }

        BigDecimal totalAmount = cart.getItems().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String orderNumber = generateOrderNumber();

        Order order = orderMapper.toOrder(user, orderNumber, totalAmount,
                request.getShippingAddress(), cart.getItems());

        Order savedOrder = orderRepository.save(order);

        cartService.clearCartAfterOrder(userId);
        log.info("Cart cleared for user: {} after placing order: {}", userId, orderNumber);

        log.info("Order created with inventory reserved: {} for user: {}", orderNumber, userId);
        
        // Publish order placed event for email notification
        eventPublisher.publishEvent(new OrderPlacedEvent(
                this,
                savedOrder.getOrderNumber(),
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                savedOrder.getTotalAmount()
        ));
        log.info("OrderPlacedEvent published for order: {}", savedOrder.getOrderNumber());

        return orderMapper.toOrderResponse(savedOrder);
    }

    /**
     * Updates order with payment intent ID
     */
    @Transactional
    public void updateOrderPaymentIntentId(UUID orderId, String paymentIntentId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order with ID " + orderId + " not found"));

        order.setStripePaymentIntentId(paymentIntentId);
        orderRepository.save(order);

        log.info("Order {} updated with payment intent ID: {}", orderId, paymentIntentId);
    }

    /**
     * Updates order status by payment intent ID (called by webhook)
     */
    @Transactional
    public void updateOrderStatusByPaymentIntentId(String paymentIntentId, OrderStatus newStatus) {
        Order order = orderRepository.findByStripePaymentIntentId(paymentIntentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order with payment intent ID " + paymentIntentId + " not found"));

        log.info("Updating order {} status from {} to {} via webhook for payment intent: {}",
                order.getId(), order.getStatus(), newStatus, paymentIntentId);

        order.setStatus(newStatus);
        orderRepository.save(order);

        // If payment failed or was cancelled, we might want to restore the cart
        // For now, we just update the status
    }

    /**
     * Cancels an order and releases inventory.
     * Evicts dashboard stats cache since order status changes.
     */
    @Transactional
    @CacheEvict(value = CacheConfig.DASHBOARD_STATS_CACHE, allEntries = true)
    public OrderResponse cancelOrder(UUID orderId, UUID userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order with ID " + orderId + " not found"));

        if (!order.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You are not authorized to cancel this order");
        }

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new BadRequestException("Order is already cancelled");
        }

        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new BadRequestException("Cannot cancel a delivered order");
        }

        for (OrderItem orderItem : order.getItems()) {
            productService.releaseInventory(orderItem.getProduct().getId(), orderItem.getQuantity());
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order savedOrder = orderRepository.save(order);

        log.info("Order {} cancelled and inventory released for user: {}", orderId, userId);

        return orderMapper.toOrderResponse(savedOrder);
    }

    public BigDecimal getTotalRevenue() {
        BigDecimal revenue = orderRepository.calculateTotalRevenue();
        return revenue != null ? revenue : BigDecimal.ZERO;
    }

    public Integer getTotalSalesByProductId(UUID productId) {
        return orderRepository.getTotalSalesByProductId(productId);
    }

    @Transactional(readOnly = true)
    public Page<Order> getAllOrdersPage(String statusStr, LocalDateTime startDate,
                                        LocalDateTime endDate, Integer page, Integer limit) {
        OrderStatus status = null;
        if (statusStr != null && !statusStr.isEmpty()) {
            try {
                status = OrderStatus.valueOf(statusStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid order status provided: '{}'. Ignoring status filter.", statusStr);
            }
        }

        int pageNumber = page != null ? page : 0;
        int pageSize = limit != null ? limit : 10;

        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        return orderRepository.findAllOrdersWithFilters(status, startDate, endDate, pageable);
    }

    /**
     * Updates order status (admin operation).
     * Evicts dashboard stats cache since pending orders count may change.
     */
    @Transactional
    @CacheEvict(value = CacheConfig.DASHBOARD_STATS_CACHE, allEntries = true)
    public OrderResponse updateOrderStatus(UUID orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order with ID " + orderId + " not found"));

        log.info("Updating order {} status from {} to {}", orderId, order.getStatus(), newStatus);

        order.setStatus(newStatus);
        Order savedOrder = orderRepository.save(order);
        
        // Fetch user data for events
        User user = savedOrder.getUser();

        // Publish appropriate events based on new status
        if (newStatus == OrderStatus.SHIPPED) {
            String trackingNumber = "TRK" + System.currentTimeMillis(); // Generate or retrieve tracking number
            eventPublisher.publishEvent(new OrderShippedEvent(
                    this,
                    savedOrder.getOrderNumber(),
                    user.getId(),
                    user.getEmail(),
                    user.getFirstName(),
                    trackingNumber
            ));
            log.info("OrderShippedEvent published for order: {}", savedOrder.getOrderNumber());
        } else if (newStatus == OrderStatus.DELIVERED) {
            eventPublisher.publishEvent(new OrderDeliveredEvent(
                    this,
                    savedOrder.getOrderNumber(),
                    user.getId(),
                    user.getEmail(),
                    user.getFirstName()
            ));
            log.info("OrderDeliveredEvent published for order: {}", savedOrder.getOrderNumber());
        }

        return orderMapper.toOrderResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderStats getUserOrderStats(UUID userId) {
        Long totalOrders = orderRepository.countTotalOrdersByUser(userId);
        Long pendingCount = orderRepository.countOrdersByUserAndStatus(userId, OrderStatus.PENDING);
        Long paidCount = orderRepository.countOrdersByUserAndStatus(userId, OrderStatus.PAID);
        Long processingCount = orderRepository.countOrdersByUserAndStatus(userId, OrderStatus.PROCESSING);
        Long shippedCount = orderRepository.countOrdersByUserAndStatus(userId, OrderStatus.SHIPPED);
        Long deliveredCount = orderRepository.countOrdersByUserAndStatus(userId, OrderStatus.DELIVERED);
        Long cancelledCount = orderRepository.countOrdersByUserAndStatus(userId, OrderStatus.CANCELLED);
        BigDecimal totalSpent = orderRepository.calculateTotalSpentByUser(userId);

        return orderMapper.toOrderStats(totalOrders, pendingCount, paidCount, processingCount,
                shippedCount, deliveredCount, cancelledCount, totalSpent);
    }

    private String generateOrderNumber() {
        String datePart = LocalDateTime.now().toString().substring(0, 10).replace("-", "");
        String randomPart = String.format("%05d", (int) (Math.random() * 100000));
        return "ORD-" + datePart + "-" + randomPart;
    }

    /**
     * Find orders with a specific status updated before the cutoff date.
     * Used by scheduled tasks for auto-delivery updates.
     */
    @Transactional(readOnly = true)
    public List<Order> findOrdersByStatusAndUpdatedBefore(OrderStatus status, LocalDateTime cutoffDate) {
        return orderRepository.findByStatusAndUpdatedBefore(status, cutoffDate);
    }

    /**
     * Find delivered orders without reviews within a timeframe.
     * Used by scheduled tasks for review reminders.
     */
    @Transactional(readOnly = true)
    public List<Order> findDeliveredOrdersWithoutReviews(LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.findDeliveredOrdersWithoutReviews(startDate, endDate);
    }

    /**
     * Auto-deliver a shipped order (update status to DELIVERED).
     * Used by scheduled tasks.
     */
    @Transactional
    @CacheEvict(value = CacheConfig.DASHBOARD_STATS_CACHE, allEntries = true)
    public void autoDeliverOrder(Order order) {
        order.setStatus(OrderStatus.DELIVERED);
        Order savedOrder = orderRepository.save(order);
        
        User user = order.getUser();
        eventPublisher.publishEvent(new OrderDeliveredEvent(
                this,
                savedOrder.getOrderNumber(),
                user.getId(),
                user.getEmail(),
                user.getFirstName()
        ));
        log.info("Order {} auto-delivered", order.getId());
    }

    /**
     * Count total orders.
     * Used by scheduled tasks for reporting.
     */
    @Transactional(readOnly = true)
    public long countOrders() {
        return orderRepository.count();
    }

    /**
     * Count pending orders.
     * Used by scheduled tasks for reporting.
     */
    @Transactional(readOnly = true)
    public long countPendingOrders() {
        return orderRepository.countPendingOrders();
    }
}
