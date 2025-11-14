package app.order.service;

import app.cart.model.Cart;
import app.cart.service.CartService;
import app.cartitem.model.CartItem;
import app.exception.BadRequestException;
import app.exception.ResourceNotFoundException;
import app.exception.UnauthorizedException;
import app.order.dto.CreateOrderRequest;
import app.order.dto.OrderDTO;
import app.order.dto.OrderStatsDTO;
import app.order.dto.OrdersResponse;
import app.order.mapper.OrderMapper;
import app.order.model.Order;
import app.order.model.OrderItem;
import app.order.model.OrderStatus;
import app.order.repository.OrderRepository;
import app.user.model.User;
import app.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final CartService cartService;
    private final UserService userService;

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

        return OrdersResponse.builder()
                .orders(orderPage.getContent().stream()
                        .map(orderMapper::toOrderDTO)
                        .collect(Collectors.toList()))
                .currentPage(orderPage.getNumber())
                .totalPages(orderPage.getTotalPages())
                .totalElements(orderPage.getTotalElements())
                .size(orderPage.getSize())
                .first(orderPage.isFirst())
                .last(orderPage.isLast())
                .hasNext(orderPage.hasNext())
                .hasPrevious(orderPage.hasPrevious())
                .build();
    }

    @Transactional(readOnly = true)
    public OrderDTO getOrderById(UUID orderId, UUID userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order with ID " + orderId + " not found"));

        if (!order.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You are not authorized to view this order");
        }

        return orderMapper.toOrderDTO(order);
    }

    @Transactional
    public OrderDTO createOrder(UUID userId, CreateOrderRequest request) {
        User user = userService.getUserById(userId);

        Cart cart = cartService.getCartWithItemsForOrder(userId);

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new BadRequestException("Cart is empty. Cannot create order with no items");
        }

        BigDecimal totalAmount = cart.getItems().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String orderNumber = generateOrderNumber();

        Order order = Order.builder()
                .user(user)
                .orderNumber(orderNumber)
                .totalAmount(totalAmount)
                .status(OrderStatus.PENDING)
                .shippingAddress(request.getShippingAddress())
                .build();

        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(cartItem.getProduct())
                    .quantity(cartItem.getQuantity())
                    .price(cartItem.getPrice())
                    .build();
            orderItems.add(orderItem);
        }
        order.setItems(orderItems);

        Order savedOrder = orderRepository.save(order);

        cartService.clearCartAfterOrder(userId);
        log.info("Cart cleared for user: {} after placing order: {}", userId, orderNumber);

        log.info("Order created with PENDING status: {} for user: {}", orderNumber, userId);

        return orderMapper.toOrderDTO(savedOrder);
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

    @Transactional
    public OrderDTO cancelOrder(UUID orderId, UUID userId) {
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

        order.setStatus(OrderStatus.CANCELLED);
        Order savedOrder = orderRepository.save(order);

        return orderMapper.toOrderDTO(savedOrder);
    }

    public Long getTotalOrdersCount() {
        return orderRepository.count();
    }

    public BigDecimal getTotalRevenue() {
        BigDecimal revenue = orderRepository.calculateTotalRevenue();
        return revenue != null ? revenue : BigDecimal.ZERO;
    }

    public Long getPendingOrdersCount() {
        return orderRepository.countPendingOrders();
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

    @Transactional
    public OrderDTO updateOrderStatus(UUID orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order with ID " + orderId + " not found"));

        log.info("Updating order {} status from {} to {}", orderId, order.getStatus(), newStatus);

        order.setStatus(newStatus);
        Order savedOrder = orderRepository.save(order);

        return orderMapper.toOrderDTO(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderStatsDTO getUserOrderStats(UUID userId) {
        Long totalOrders = orderRepository.countTotalOrdersByUser(userId);
        Long pendingCount = orderRepository.countOrdersByUserAndStatus(userId, OrderStatus.PENDING);
        Long paidCount = orderRepository.countOrdersByUserAndStatus(userId, OrderStatus.PAID);
        Long processingCount = orderRepository.countOrdersByUserAndStatus(userId, OrderStatus.PROCESSING);
        Long shippedCount = orderRepository.countOrdersByUserAndStatus(userId, OrderStatus.SHIPPED);
        Long deliveredCount = orderRepository.countOrdersByUserAndStatus(userId, OrderStatus.DELIVERED);
        Long cancelledCount = orderRepository.countOrdersByUserAndStatus(userId, OrderStatus.CANCELLED);
        BigDecimal totalSpent = orderRepository.calculateTotalSpentByUser(userId);

        return OrderStatsDTO.builder()
                .totalOrders(totalOrders)
                .pendingCount(pendingCount)
                .paidCount(paidCount)
                .processingCount(processingCount)
                .shippedCount(shippedCount)
                .deliveredCount(deliveredCount)
                .cancelledCount(cancelledCount)
                .totalSpent(totalSpent)
                .build();
    }

    private String generateOrderNumber() {
        String datePart = LocalDateTime.now().toString().substring(0, 10).replace("-", "");
        String randomPart = String.format("%05d", (int) (Math.random() * 100000));
        return "ORD-" + datePart + "-" + randomPart;
    }
}
