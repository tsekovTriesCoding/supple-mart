package app.order.service;

import app.cart.model.Cart;
import app.cart.service.CartService;
import app.cartitem.model.CartItem;
import app.exception.BadRequestException;
import app.exception.ResourceNotFoundException;
import app.exception.UnauthorizedException;
import app.order.dto.CreateOrderRequest;
import app.order.dto.OrderResponse;
import app.order.dto.OrderStats;
import app.order.dto.OrdersResponse;
import app.order.mapper.OrderMapper;
import app.order.model.Order;
import app.order.model.OrderItem;
import app.order.model.OrderStatus;
import app.order.repository.OrderRepository;
import app.product.model.Category;
import app.product.model.Product;
import app.product.service.ProductService;
import app.user.model.AuthProvider;
import app.user.model.Role;
import app.user.model.User;
import app.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService Unit Tests")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private CartService cartService;

    @Mock
    private UserService userService;

    @Mock
    private ProductService productService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private OrderService orderService;

    private User testUser;
    private Product testProduct;
    private Cart testCart;
    private Order testOrder;
    private UUID userId;
    private UUID productId;
    private UUID orderId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        productId = UUID.randomUUID();
        orderId = UUID.randomUUID();

        testUser = User.builder()
                .id(userId)
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .role(Role.CUSTOMER)
                .authProvider(AuthProvider.LOCAL)
                .build();

        testProduct = Product.builder()
                .id(productId)
                .name("Test Product")
                .price(new BigDecimal("29.99"))
                .category(Category.PROTEIN)
                .stockQuantity(50)
                .isActive(true)
                .build();

        CartItem cartItem = CartItem.builder()
                .id(UUID.randomUUID())
                .product(testProduct)
                .quantity(2)
                .price(testProduct.getPrice())
                .build();

        testCart = Cart.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .items(new ArrayList<>(List.of(cartItem)))
                .build();
        cartItem.setCart(testCart);

        OrderItem orderItem = OrderItem.builder()
                .id(UUID.randomUUID())
                .product(testProduct)
                .quantity(2)
                .price(testProduct.getPrice())
                .build();

        testOrder = Order.builder()
                .id(orderId)
                .user(testUser)
                .orderNumber("ORD-20231201-12345")
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("59.98"))
                .shippingAddress("123 Test St")
                .items(new ArrayList<>(List.of(orderItem)))
                .createdAt(LocalDateTime.now())
                .build();
        orderItem.setOrder(testOrder);
    }

    @Nested
    @DisplayName("getUserOrders Tests")
    class GetUserOrdersTests {

        @Test
        @DisplayName("Should return paginated orders for user")
        void getUserOrders_WithValidUser_ReturnsOrders() {
            Page<Order> orderPage = new PageImpl<>(List.of(testOrder));
            OrdersResponse expectedResponse = OrdersResponse.builder().build();

            when(orderRepository.findUserOrdersWithFilters(eq(userId), any(), any(), any(), any(Pageable.class)))
                    .thenReturn(orderPage);
            when(orderMapper.toOrdersResponse(orderPage)).thenReturn(expectedResponse);

            OrdersResponse result = orderService.getUserOrders(userId, null, null, null, 0, 10);

            assertThat(result).isNotNull();
            verify(orderRepository).findUserOrdersWithFilters(eq(userId), any(), any(), any(), any(Pageable.class));
        }

        @Test
        @DisplayName("Should filter orders by status")
        void getUserOrders_WithStatusFilter_ReturnsFilteredOrders() {
            Page<Order> orderPage = new PageImpl<>(List.of(testOrder));
            OrdersResponse expectedResponse = OrdersResponse.builder().build();

            when(orderRepository.findUserOrdersWithFilters(eq(userId), eq(OrderStatus.PENDING), any(), any(), any(Pageable.class)))
                    .thenReturn(orderPage);
            when(orderMapper.toOrdersResponse(orderPage)).thenReturn(expectedResponse);

            OrdersResponse result = orderService.getUserOrders(userId, "PENDING", null, null, 0, 10);

            assertThat(result).isNotNull();
            verify(orderRepository).findUserOrdersWithFilters(eq(userId), eq(OrderStatus.PENDING), any(), any(), any(Pageable.class));
        }

        @Test
        @DisplayName("Should handle invalid status gracefully")
        void getUserOrders_WithInvalidStatus_IgnoresStatusFilter() {
            Page<Order> orderPage = new PageImpl<>(List.of(testOrder));
            OrdersResponse expectedResponse = OrdersResponse.builder().build();

            when(orderRepository.findUserOrdersWithFilters(eq(userId), isNull(), any(), any(), any(Pageable.class)))
                    .thenReturn(orderPage);
            when(orderMapper.toOrdersResponse(orderPage)).thenReturn(expectedResponse);

            OrdersResponse result = orderService.getUserOrders(userId, "INVALID_STATUS", null, null, 0, 10);

            assertThat(result).isNotNull();
            verify(orderRepository).findUserOrdersWithFilters(eq(userId), isNull(), any(), any(), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("getOrderById Tests")
    class GetOrderByIdTests {

        @Test
        @DisplayName("Should return order when user is owner")
        void getOrderById_WithOwner_ReturnsOrder() {
            OrderResponse expectedResponse = OrderResponse.builder().build();
            when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
            when(orderMapper.toOrderResponse(testOrder)).thenReturn(expectedResponse);

            OrderResponse result = orderService.getOrderById(orderId, userId);

            assertThat(result).isNotNull();
            verify(orderRepository).findById(orderId);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when order not found")
        void getOrderById_WithNonExistentOrder_ThrowsException() {
            UUID nonExistentId = UUID.randomUUID();
            when(orderRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.getOrderById(nonExistentId, userId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining(nonExistentId.toString());
        }

        @Test
        @DisplayName("Should throw UnauthorizedException when user is not owner")
        void getOrderById_WithNonOwner_ThrowsException() {
            UUID differentUserId = UUID.randomUUID();
            when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));

            assertThatThrownBy(() -> orderService.getOrderById(orderId, differentUserId))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("not authorized");
        }
    }

    @Nested
    @DisplayName("createOrder Tests")
    class CreateOrderTests {

        @Test
        @DisplayName("Should create order successfully with valid cart")
        void createOrder_WithValidCart_CreatesOrder() {
            CreateOrderRequest request = new CreateOrderRequest();
            request.setShippingAddress("123 Test St");

            OrderResponse expectedResponse = OrderResponse.builder().build();

            when(userService.getUserById(userId)).thenReturn(testUser);
            when(cartService.getCartWithItemsForOrder(userId)).thenReturn(testCart);
            when(orderMapper.toOrder(eq(testUser), anyString(), any(BigDecimal.class), eq(request.getShippingAddress()), any()))
                    .thenReturn(testOrder);
            when(orderRepository.save(testOrder)).thenReturn(testOrder);
            when(orderMapper.toOrderResponse(testOrder)).thenReturn(expectedResponse);

            OrderResponse result = orderService.createOrder(userId, request);

            assertThat(result).isNotNull();
            verify(productService).reserveInventory(eq(productId), eq(2));
            verify(cartService).clearCartAfterOrder(userId);
            verify(eventPublisher).publishEvent(any());
        }

        @Test
        @DisplayName("Should throw BadRequestException when cart is empty")
        void createOrder_WithEmptyCart_ThrowsException() {
            CreateOrderRequest request = new CreateOrderRequest();
            request.setShippingAddress("123 Test St");

            Cart emptyCart = Cart.builder()
                    .id(UUID.randomUUID())
                    .user(testUser)
                    .items(new ArrayList<>())
                    .build();

            when(userService.getUserById(userId)).thenReturn(testUser);
            when(cartService.getCartWithItemsForOrder(userId)).thenReturn(emptyCart);

            assertThatThrownBy(() -> orderService.createOrder(userId, request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Cart is empty");
        }

        @Test
        @DisplayName("Should throw BadRequestException when cart items is null")
        void createOrder_WithNullCartItems_ThrowsException() {
            CreateOrderRequest request = new CreateOrderRequest();
            request.setShippingAddress("123 Test St");

            Cart nullItemsCart = Cart.builder()
                    .id(UUID.randomUUID())
                    .user(testUser)
                    .items(null)
                    .build();

            when(userService.getUserById(userId)).thenReturn(testUser);
            when(cartService.getCartWithItemsForOrder(userId)).thenReturn(nullItemsCart);

            assertThatThrownBy(() -> orderService.createOrder(userId, request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Cart is empty");
        }
    }

    @Nested
    @DisplayName("cancelOrder Tests")
    class CancelOrderTests {

        @Test
        @DisplayName("Should cancel order successfully")
        void cancelOrder_WithValidOrder_CancelsOrder() {
            OrderResponse expectedResponse = OrderResponse.builder().build();

            when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
            when(orderRepository.save(testOrder)).thenReturn(testOrder);
            when(orderMapper.toOrderResponse(testOrder)).thenReturn(expectedResponse);

            OrderResponse result = orderService.cancelOrder(orderId, userId);

            assertThat(result).isNotNull();
            assertThat(testOrder.getStatus()).isEqualTo(OrderStatus.CANCELLED);
            verify(productService).releaseInventory(eq(productId), eq(2));
        }

        @Test
        @DisplayName("Should throw UnauthorizedException when user is not owner")
        void cancelOrder_WithNonOwner_ThrowsException() {
            UUID differentUserId = UUID.randomUUID();
            when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));

            assertThatThrownBy(() -> orderService.cancelOrder(orderId, differentUserId))
                    .isInstanceOf(UnauthorizedException.class);
        }

        @Test
        @DisplayName("Should throw BadRequestException when order is already cancelled")
        void cancelOrder_WithAlreadyCancelled_ThrowsException() {
            testOrder.setStatus(OrderStatus.CANCELLED);
            when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));

            assertThatThrownBy(() -> orderService.cancelOrder(orderId, userId))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("already cancelled");
        }

        @Test
        @DisplayName("Should throw BadRequestException when order is delivered")
        void cancelOrder_WithDeliveredOrder_ThrowsException() {
            testOrder.setStatus(OrderStatus.DELIVERED);
            when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));

            assertThatThrownBy(() -> orderService.cancelOrder(orderId, userId))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("delivered order");
        }
    }

    @Nested
    @DisplayName("updateOrderStatus Tests")
    class UpdateOrderStatusTests {

        @Test
        @DisplayName("Should update order status to SHIPPED and publish event")
        void updateOrderStatus_ToShipped_PublishesEvent() {
            OrderResponse expectedResponse = OrderResponse.builder().build();

            when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
            when(orderRepository.save(testOrder)).thenReturn(testOrder);
            when(orderMapper.toOrderResponse(testOrder)).thenReturn(expectedResponse);

            OrderResponse result = orderService.updateOrderStatus(orderId, OrderStatus.SHIPPED);

            assertThat(result).isNotNull();
            assertThat(testOrder.getStatus()).isEqualTo(OrderStatus.SHIPPED);
            verify(eventPublisher).publishEvent(any());
        }

        @Test
        @DisplayName("Should update order status to DELIVERED and publish event")
        void updateOrderStatus_ToDelivered_PublishesEvent() {
            testOrder.setStatus(OrderStatus.SHIPPED);
            OrderResponse expectedResponse = OrderResponse.builder().build();

            when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
            when(orderRepository.save(testOrder)).thenReturn(testOrder);
            when(orderMapper.toOrderResponse(testOrder)).thenReturn(expectedResponse);

            OrderResponse result = orderService.updateOrderStatus(orderId, OrderStatus.DELIVERED);

            assertThat(result).isNotNull();
            assertThat(testOrder.getStatus()).isEqualTo(OrderStatus.DELIVERED);
            verify(eventPublisher).publishEvent(any());
        }

        @Test
        @DisplayName("Should update status without publishing event for non-shipped/delivered")
        void updateOrderStatus_ToPaid_NoEventPublished() {
            OrderResponse expectedResponse = OrderResponse.builder().build();

            when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
            when(orderRepository.save(testOrder)).thenReturn(testOrder);
            when(orderMapper.toOrderResponse(testOrder)).thenReturn(expectedResponse);

            OrderResponse result = orderService.updateOrderStatus(orderId, OrderStatus.PAID);

            assertThat(result).isNotNull();
            assertThat(testOrder.getStatus()).isEqualTo(OrderStatus.PAID);
            verify(eventPublisher, never()).publishEvent(any());
        }
    }

    @Nested
    @DisplayName("updateOrderPaymentIntentId Tests")
    class UpdateOrderPaymentIntentIdTests {

        @Test
        @DisplayName("Should update payment intent ID successfully")
        void updateOrderPaymentIntentId_WithValidOrder_UpdatesId() {
            String paymentIntentId = "pi_test123";
            when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
            when(orderRepository.save(testOrder)).thenReturn(testOrder);

            orderService.updateOrderPaymentIntentId(orderId, paymentIntentId);

            assertThat(testOrder.getStripePaymentIntentId()).isEqualTo(paymentIntentId);
            verify(orderRepository).save(testOrder);
        }
    }

    @Nested
    @DisplayName("updateOrderStatusByPaymentIntentId Tests")
    class UpdateOrderStatusByPaymentIntentIdTests {

        @Test
        @DisplayName("Should update status by payment intent ID")
        void updateOrderStatusByPaymentIntentId_WithValidIntent_UpdatesStatus() {
            String paymentIntentId = "pi_test123";
            testOrder.setStripePaymentIntentId(paymentIntentId);

            when(orderRepository.findByStripePaymentIntentId(paymentIntentId)).thenReturn(Optional.of(testOrder));
            when(orderRepository.save(testOrder)).thenReturn(testOrder);

            orderService.updateOrderStatusByPaymentIntentId(paymentIntentId, OrderStatus.PAID);

            assertThat(testOrder.getStatus()).isEqualTo(OrderStatus.PAID);
            verify(orderRepository).save(testOrder);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when payment intent not found")
        void updateOrderStatusByPaymentIntentId_WithInvalidIntent_ThrowsException() {
            String invalidPaymentIntentId = "pi_invalid";
            when(orderRepository.findByStripePaymentIntentId(invalidPaymentIntentId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.updateOrderStatusByPaymentIntentId(invalidPaymentIntentId, OrderStatus.PAID))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getTotalRevenue Tests")
    class GetTotalRevenueTests {

        @Test
        @DisplayName("Should return total revenue")
        void getTotalRevenue_WithOrders_ReturnsRevenue() {
            BigDecimal expectedRevenue = new BigDecimal("1000.00");
            when(orderRepository.calculateTotalRevenue()).thenReturn(expectedRevenue);

            BigDecimal result = orderService.getTotalRevenue();

            assertThat(result).isEqualTo(expectedRevenue);
        }

        @Test
        @DisplayName("Should return zero when no orders exist")
        void getTotalRevenue_WithNoOrders_ReturnsZero() {
            when(orderRepository.calculateTotalRevenue()).thenReturn(null);

            BigDecimal result = orderService.getTotalRevenue();

            assertThat(result).isEqualTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("getUserOrderStats Tests")
    class GetUserOrderStatsTests {

        @Test
        @DisplayName("Should return user order statistics")
        void getUserOrderStats_WithOrders_ReturnsStats() {
            OrderStats expectedStats = new OrderStats();

            when(orderRepository.countTotalOrdersByUser(userId)).thenReturn(10L);
            when(orderRepository.countOrdersByUserAndStatus(userId, OrderStatus.PENDING)).thenReturn(2L);
            when(orderRepository.countOrdersByUserAndStatus(userId, OrderStatus.PAID)).thenReturn(3L);
            when(orderRepository.countOrdersByUserAndStatus(userId, OrderStatus.PROCESSING)).thenReturn(1L);
            when(orderRepository.countOrdersByUserAndStatus(userId, OrderStatus.SHIPPED)).thenReturn(2L);
            when(orderRepository.countOrdersByUserAndStatus(userId, OrderStatus.DELIVERED)).thenReturn(1L);
            when(orderRepository.countOrdersByUserAndStatus(userId, OrderStatus.CANCELLED)).thenReturn(1L);
            when(orderRepository.calculateTotalSpentByUser(userId)).thenReturn(new BigDecimal("500.00"));
            when(orderMapper.toOrderStats(any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(expectedStats);

            OrderStats result = orderService.getUserOrderStats(userId);

            assertThat(result).isNotNull();
            verify(orderRepository).countTotalOrdersByUser(userId);
            verify(orderRepository, times(6)).countOrdersByUserAndStatus(eq(userId), any());
        }
    }

    @Nested
    @DisplayName("getTotalSalesByProductId Tests")
    class GetTotalSalesByProductIdTests {

        @Test
        @DisplayName("Should return total sales for product")
        void getTotalSalesByProductId_WithSales_ReturnsTotalSales() {
            when(orderRepository.getTotalSalesByProductId(productId)).thenReturn(50);

            Integer result = orderService.getTotalSalesByProductId(productId);

            assertThat(result).isEqualTo(50);
            verify(orderRepository).getTotalSalesByProductId(productId);
        }
    }
}
