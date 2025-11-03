package app.order.service;

import app.cart.model.Cart;
import app.cart.service.CartService;
import app.cartitem.model.CartItem;
import app.order.dto.CreateOrderRequest;
import app.order.dto.OrderDTO;
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

    @Transactional
    public OrderDTO createOrder(UUID userId, CreateOrderRequest request) {
        User user = userService.getUserById(userId);

        Cart cart = cartService.getCartWithItemsForOrder(userId);

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
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

        return orderMapper.toOrderDTO(savedOrder);
    }

    private String generateOrderNumber() {
        String datePart = LocalDateTime.now().toString().substring(0, 10).replace("-", "");
        String randomPart = String.format("%05d", (int) (Math.random() * 100000));
        return "ORD-" + datePart + "-" + randomPart;
    }
}
