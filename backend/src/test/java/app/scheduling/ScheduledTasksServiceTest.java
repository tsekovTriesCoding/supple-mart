package app.scheduling;

import app.cart.model.Cart;
import app.cart.service.CartService;
import app.cartitem.model.CartItem;
import app.notification.event.AbandonedCartEvent;
import app.notification.event.DailyReportEvent;
import app.notification.event.LowStockAlertEvent;
import app.notification.event.ReviewReminderEvent;
import app.order.model.Order;
import app.order.model.OrderStatus;
import app.order.service.OrderService;
import app.product.model.Product;
import app.product.service.ProductService;
import app.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduledTasksServiceTest {

    @Mock
    private CartService cartService;

    @Mock
    private OrderService orderService;

    @Mock
    private ProductService productService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ScheduledTasksService scheduledTasksService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(scheduledTasksService, "abandonedCartHours", 24);
        ReflectionTestUtils.setField(scheduledTasksService, "autoDeliverDays", 7);
        ReflectionTestUtils.setField(scheduledTasksService, "reviewReminderMinDays", 3);
        ReflectionTestUtils.setField(scheduledTasksService, "reviewReminderMaxDays", 14);
        ReflectionTestUtils.setField(scheduledTasksService, "lowStockThreshold", 10);
    }

    @Nested
    @DisplayName("Abandoned Cart Processing Tests")
    class AbandonedCartTests {

        @Test
        @DisplayName("Should publish events for abandoned carts")
        void shouldPublishEventsForAbandonedCarts() {
            User user = createTestUser();
            Product product = createTestProduct("Test Product", BigDecimal.valueOf(29.99), 100);
            Cart cart = createTestCart(user, product, 2);

            when(cartService.findAbandonedCarts(any(LocalDateTime.class)))
                    .thenReturn(List.of(cart));

            scheduledTasksService.processAbandonedCarts();

            verify(eventPublisher).publishEvent(any(AbandonedCartEvent.class));
        }

        @Test
        @DisplayName("Should not publish events when no abandoned carts")
        void shouldNotPublishEventsWhenNoAbandonedCarts() {
            when(cartService.findAbandonedCarts(any(LocalDateTime.class)))
                    .thenReturn(Collections.emptyList());

            scheduledTasksService.processAbandonedCarts();

            verify(eventPublisher, never()).publishEvent(any(AbandonedCartEvent.class));
        }

        @Test
        @DisplayName("Should calculate correct cart total in abandoned cart event")
        void shouldCalculateCorrectCartTotal() {
            User user = createTestUser();
            Product product = createTestProduct("Test Product", BigDecimal.valueOf(10.00), 100);
            Cart cart = createTestCart(user, product, 3);

            when(cartService.findAbandonedCarts(any(LocalDateTime.class)))
                    .thenReturn(List.of(cart));

            scheduledTasksService.processAbandonedCarts();

            ArgumentCaptor<AbandonedCartEvent> captor = ArgumentCaptor.forClass(AbandonedCartEvent.class);
            verify(eventPublisher).publishEvent(captor.capture());
            AbandonedCartEvent event = captor.getValue();

            assertThat(event.getCartTotal()).isEqualByComparingTo(BigDecimal.valueOf(30.00));
        }

        @Test
        @DisplayName("Should continue processing other carts when one fails")
        void shouldContinueProcessingWhenOneFails() {
            User user = createTestUser();
            Product product = createTestProduct("Test Product", BigDecimal.valueOf(29.99), 100);
            Cart cart1 = createTestCart(user, product, 1);
            Cart cart2 = createTestCart(user, product, 2);

            when(cartService.findAbandonedCarts(any(LocalDateTime.class)))
                    .thenReturn(List.of(cart1, cart2));
            doThrow(new RuntimeException("Event publish failed"))
                    .doNothing()
                    .when(eventPublisher).publishEvent(any(AbandonedCartEvent.class));

            scheduledTasksService.processAbandonedCarts();

            verify(eventPublisher, times(2)).publishEvent(any(AbandonedCartEvent.class));
        }
    }

    @Nested
    @DisplayName("Low Stock Alert Tests")
    class LowStockAlertTests {

        @Test
        @DisplayName("Should publish event when low stock products exist")
        void shouldPublishEventWhenLowStockProductsExist() {
            Product lowStockProduct = createTestProduct("Low Stock Product", BigDecimal.valueOf(19.99), 5);

            when(productService.findLowStockProducts(10)).thenReturn(List.of(lowStockProduct));
            when(productService.findOutOfStockProducts()).thenReturn(Collections.emptyList());

            scheduledTasksService.checkLowStockProducts();

            verify(eventPublisher).publishEvent(any(LowStockAlertEvent.class));
        }

        @Test
        @DisplayName("Should publish event when out of stock products exist")
        void shouldPublishEventWhenOutOfStockProductsExist() {
            Product outOfStockProduct = createTestProduct("Out of Stock", BigDecimal.valueOf(9.99), 0);

            when(productService.findLowStockProducts(10)).thenReturn(Collections.emptyList());
            when(productService.findOutOfStockProducts()).thenReturn(List.of(outOfStockProduct));

            scheduledTasksService.checkLowStockProducts();

            verify(eventPublisher).publishEvent(any(LowStockAlertEvent.class));
        }

        @Test
        @DisplayName("Should not publish event when no stock issues")
        void shouldNotPublishEventWhenNoStockIssues() {
            when(productService.findLowStockProducts(10)).thenReturn(Collections.emptyList());
            when(productService.findOutOfStockProducts()).thenReturn(Collections.emptyList());

            scheduledTasksService.checkLowStockProducts();

            verify(eventPublisher, never()).publishEvent(any(LowStockAlertEvent.class));
        }

        @Test
        @DisplayName("Should handle exception during event publishing")
        void shouldHandleExceptionDuringEventPublishing() {
            Product lowStockProduct = createTestProduct("Low Stock", BigDecimal.valueOf(19.99), 5);

            when(productService.findLowStockProducts(10)).thenReturn(List.of(lowStockProduct));
            when(productService.findOutOfStockProducts()).thenReturn(Collections.emptyList());
            doThrow(new RuntimeException("Publish failed"))
                    .when(eventPublisher).publishEvent(any(LowStockAlertEvent.class));

            scheduledTasksService.checkLowStockProducts();

            verify(eventPublisher).publishEvent(any(LowStockAlertEvent.class));
        }
    }

    @Nested
    @DisplayName("Auto-Deliver Shipped Orders Tests")
    class AutoDeliverTests {

        @Test
        @DisplayName("Should auto-deliver shipped orders")
        void shouldAutoDeliverShippedOrders() {
            Order shippedOrder = createTestOrder(OrderStatus.SHIPPED);

            when(orderService.findOrdersByStatusAndUpdatedBefore(eq(OrderStatus.SHIPPED), any(LocalDateTime.class)))
                    .thenReturn(List.of(shippedOrder));

            scheduledTasksService.autoDeliverShippedOrders();

            verify(orderService).autoDeliverOrder(shippedOrder);
        }

        @Test
        @DisplayName("Should handle no shipped orders")
        void shouldHandleNoShippedOrders() {
            when(orderService.findOrdersByStatusAndUpdatedBefore(eq(OrderStatus.SHIPPED), any(LocalDateTime.class)))
                    .thenReturn(Collections.emptyList());

            scheduledTasksService.autoDeliverShippedOrders();

            verify(orderService, never()).autoDeliverOrder(any());
        }

        @Test
        @DisplayName("Should continue processing when one order fails")
        void shouldContinueProcessingWhenOneFails() {
            Order order1 = createTestOrder(OrderStatus.SHIPPED);
            Order order2 = createTestOrder(OrderStatus.SHIPPED);

            when(orderService.findOrdersByStatusAndUpdatedBefore(eq(OrderStatus.SHIPPED), any(LocalDateTime.class)))
                    .thenReturn(List.of(order1, order2));
            doThrow(new RuntimeException("Delivery failed"))
                    .doNothing()
                    .when(orderService).autoDeliverOrder(any());

            scheduledTasksService.autoDeliverShippedOrders();

            verify(orderService, times(2)).autoDeliverOrder(any());
        }
    }

    @Nested
    @DisplayName("Review Reminder Tests")
    class ReviewReminderTests {

        @Test
        @DisplayName("Should send review reminders for delivered orders")
        void shouldSendReviewRemindersForDeliveredOrders() {
            Order deliveredOrder = createTestOrder(OrderStatus.DELIVERED);

            when(orderService.findDeliveredOrdersWithoutReviews(any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(List.of(deliveredOrder));

            scheduledTasksService.sendReviewReminders();

            verify(eventPublisher).publishEvent(any(ReviewReminderEvent.class));
        }

        @Test
        @DisplayName("Should not send reminders when no orders without reviews")
        void shouldNotSendRemindersWhenNoOrdersWithoutReviews() {
            when(orderService.findDeliveredOrdersWithoutReviews(any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(Collections.emptyList());

            scheduledTasksService.sendReviewReminders();

            verify(eventPublisher, never()).publishEvent(any(ReviewReminderEvent.class));
        }

        @Test
        @DisplayName("Should continue processing when one reminder fails")
        void shouldContinueProcessingWhenOneReminderFails() {
            Order order1 = createTestOrder(OrderStatus.DELIVERED);
            Order order2 = createTestOrder(OrderStatus.DELIVERED);

            when(orderService.findDeliveredOrdersWithoutReviews(any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(List.of(order1, order2));
            doThrow(new RuntimeException("Reminder failed"))
                    .doNothing()
                    .when(eventPublisher).publishEvent(any(ReviewReminderEvent.class));

            scheduledTasksService.sendReviewReminders();

            verify(eventPublisher, times(2)).publishEvent(any(ReviewReminderEvent.class));
        }
    }

    @Nested
    @DisplayName("Daily Report Tests")
    class DailyReportTests {

        @Test
        @DisplayName("Should generate daily report")
        void shouldGenerateDailyReport() {
            when(orderService.countOrders()).thenReturn(100L);
            when(orderService.countPendingOrders()).thenReturn(10L);
            when(productService.countLowStockProducts()).thenReturn(5L);

            scheduledTasksService.generateDailyReport();

            ArgumentCaptor<DailyReportEvent> captor = ArgumentCaptor.forClass(DailyReportEvent.class);
            verify(eventPublisher).publishEvent(captor.capture());
            DailyReportEvent event = captor.getValue();

            assertThat(event.getTotalOrders()).isEqualTo(100L);
            assertThat(event.getPendingOrders()).isEqualTo(10L);
            assertThat(event.getLowStockCount()).isEqualTo(5L);
        }

        @Test
        @DisplayName("Should handle exception during daily report generation")
        void shouldHandleExceptionDuringDailyReportGeneration() {
            when(orderService.countOrders()).thenThrow(new RuntimeException("Database error"));

            scheduledTasksService.generateDailyReport();

            verify(eventPublisher, never()).publishEvent(any(DailyReportEvent.class));
        }
    }

    @Nested
    @DisplayName("Session Cleanup Tests")
    class SessionCleanupTests {

        @Test
        @DisplayName("Should execute session cleanup without errors")
        void shouldExecuteSessionCleanupWithoutErrors() {
            scheduledTasksService.cleanupExpiredSessions();
            // No exceptions should be thrown
        }
    }

    @Nested
    @DisplayName("Health Check Tests")
    class HealthCheckTests {

        @Test
        @DisplayName("Should perform health check successfully")
        void shouldPerformHealthCheckSuccessfully() {
            when(productService.countProducts()).thenReturn(50L);

            scheduledTasksService.performHealthCheck();

            verify(productService).countProducts();
        }

        @Test
        @DisplayName("Should handle health check failure gracefully")
        void shouldHandleHealthCheckFailureGracefully() {
            when(productService.countProducts()).thenThrow(new RuntimeException("Database error"));

            scheduledTasksService.performHealthCheck();

            verify(productService).countProducts();
        }
    }

    private User createTestUser() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@example.com");
        user.setFirstName("Test");
        return user;
    }

    private Product createTestProduct(String name, BigDecimal price, int stock) {
        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setName(name);
        product.setPrice(price);
        product.setStockQuantity(stock);
        return product;
    }

    private Cart createTestCart(User user, Product product, int quantity) {
        Cart cart = new Cart();
        cart.setId(UUID.randomUUID());
        cart.setUser(user);

        CartItem item = new CartItem();
        item.setId(UUID.randomUUID());
        item.setProduct(product);
        item.setQuantity(quantity);
        item.setCart(cart);

        cart.setItems(List.of(item));
        return cart;
    }

    private Order createTestOrder(OrderStatus status) {
        User user = createTestUser();

        Order order = new Order();
        order.setId(UUID.randomUUID());
        order.setOrderNumber("ORD-" + System.currentTimeMillis());
        order.setUser(user);
        order.setStatus(status);
        order.setTotalAmount(BigDecimal.valueOf(99.99));
        return order;
    }
}
