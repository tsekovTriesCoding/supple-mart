package app.monitoring;

import app.cart.service.CartService;
import app.order.service.OrderService;
import app.product.service.ProductService;
import app.user.service.UserService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class BusinessMetricsServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private ProductService productService;

    @Mock
    private OrderService orderService;

    @Mock
    private CartService cartService;

    private MeterRegistry meterRegistry;
    private BusinessMetricsService businessMetricsService;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        
        lenient().when(userService.getTotalUsersCount()).thenReturn(100L);
        lenient().when(productService.countProducts()).thenReturn(50L);
        lenient().when(productService.countLowStockProducts()).thenReturn(5L);
        lenient().when(orderService.countOrders()).thenReturn(200L);
        lenient().when(cartService.countCarts()).thenReturn(25L);
        
        businessMetricsService = new BusinessMetricsService(
                meterRegistry, userService, productService, orderService, cartService
        );
    }

    @Nested
    @DisplayName("Counter Tests")
    class CounterTests {

        @Test
        @DisplayName("Should increment orders created counter")
        void shouldIncrementOrdersCreatedCounter() {
            businessMetricsService.incrementOrdersCreated();
            businessMetricsService.incrementOrdersCreated();

            Counter counter = meterRegistry.find("supplemart_orders_total").counter();
            assertThat(counter).isNotNull();
            assertThat(counter.count()).isEqualTo(2.0);
        }

        @Test
        @DisplayName("Should increment orders created with status tag")
        void shouldIncrementOrdersCreatedWithStatus() {
            businessMetricsService.incrementOrdersCreated("pending");
            businessMetricsService.incrementOrdersCreated("completed");

            Counter pendingCounter = meterRegistry.find("supplemart_orders_created_total")
                    .tag("status", "pending").counter();
            Counter completedCounter = meterRegistry.find("supplemart_orders_created_total")
                    .tag("status", "completed").counter();

            assertThat(pendingCounter).isNotNull();
            assertThat(completedCounter).isNotNull();
        }

        @Test
        @DisplayName("Should increment user registrations counter")
        void shouldIncrementUserRegistrationsCounter() {
            businessMetricsService.incrementUserRegistrations();

            Counter counter = meterRegistry.find("supplemart_user_registrations_total").counter();
            assertThat(counter).isNotNull();
            assertThat(counter.count()).isEqualTo(1.0);
        }

        @Test
        @DisplayName("Should increment user registrations with provider tag")
        void shouldIncrementUserRegistrationsWithProvider() {
            businessMetricsService.incrementUserRegistrations("google");

            Counter counter = meterRegistry.find("supplemart_user_registrations_total")
                    .tag("provider", "google").counter();
            assertThat(counter).isNotNull();
        }

        @Test
        @DisplayName("Should increment product views counter")
        void shouldIncrementProductViewsCounter() {
            businessMetricsService.incrementProductViews();

            Counter counter = meterRegistry.find("supplemart_products_viewed_total").counter();
            assertThat(counter).isNotNull();
        }

        @Test
        @DisplayName("Should increment product views with tags")
        void shouldIncrementProductViewsWithTags() {
            businessMetricsService.incrementProductViews("prod-123", "electronics");

            Counter counter = meterRegistry.find("supplemart_products_viewed_total")
                    .tag("product_id", "prod-123")
                    .tag("category", "electronics")
                    .counter();
            assertThat(counter).isNotNull();
        }

        @Test
        @DisplayName("Should increment cart operations counter")
        void shouldIncrementCartOperationsCounter() {
            businessMetricsService.incrementCartOperations("add");
            businessMetricsService.incrementCartOperations("remove");

            Counter addCounter = meterRegistry.find("supplemart_cart_operations_total")
                    .tag("operation", "add").counter();
            Counter removeCounter = meterRegistry.find("supplemart_cart_operations_total")
                    .tag("operation", "remove").counter();

            assertThat(addCounter).isNotNull();
            assertThat(removeCounter).isNotNull();
        }

        @Test
        @DisplayName("Should increment payment success counter")
        void shouldIncrementPaymentSuccessCounter() {
            businessMetricsService.incrementPaymentSuccess();

            Counter counter = meterRegistry.find("supplemart_payments_total")
                    .tag("status", "success").counter();
            assertThat(counter).isNotNull();
            assertThat(counter.count()).isEqualTo(1.0);
        }

        @Test
        @DisplayName("Should increment payment failure counter")
        void shouldIncrementPaymentFailureCounter() {
            businessMetricsService.incrementPaymentFailure();

            Counter counter = meterRegistry.find("supplemart_payments_total")
                    .tag("status", "failure").counter();
            assertThat(counter).isNotNull();
            assertThat(counter.count()).isEqualTo(1.0);
        }

        @Test
        @DisplayName("Should increment OAuth2 logins counter")
        void shouldIncrementOAuth2LoginsCounter() {
            businessMetricsService.incrementOAuth2Logins("github");

            Counter counter = meterRegistry.find("supplemart_oauth2_logins_total")
                    .tag("provider", "github").counter();
            assertThat(counter).isNotNull();
        }
    }

    @Nested
    @DisplayName("Gauge Tests")
    class GaugeTests {

        @Test
        @DisplayName("Should set active carts count")
        void shouldSetActiveCartsCount() {
            businessMetricsService.setActiveCartsCount(10);

            Double value = meterRegistry.find("supplemart_active_carts").gauge().value();
            assertThat(value).isEqualTo(10.0);
        }

        @Test
        @DisplayName("Should increment active carts")
        void shouldIncrementActiveCarts() {
            businessMetricsService.setActiveCartsCount(5);
            businessMetricsService.incrementActiveCarts();

            Double value = meterRegistry.find("supplemart_active_carts").gauge().value();
            assertThat(value).isEqualTo(6.0);
        }

        @Test
        @DisplayName("Should decrement active carts")
        void shouldDecrementActiveCarts() {
            businessMetricsService.setActiveCartsCount(5);
            businessMetricsService.decrementActiveCarts();

            Double value = meterRegistry.find("supplemart_active_carts").gauge().value();
            assertThat(value).isEqualTo(4.0);
        }
    }

    @Nested
    @DisplayName("Timer Tests")
    class TimerTests {

        @Test
        @DisplayName("Should start and stop order processing timer")
        void shouldStartAndStopOrderProcessingTimer() {
            Timer.Sample sample = businessMetricsService.startOrderProcessingTimer();
            businessMetricsService.stopOrderProcessingTimer(sample);

            Timer timer = meterRegistry.find("supplemart_order_processing_time").timer();
            assertThat(timer).isNotNull();
            assertThat(timer.count()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should start and stop payment processing timer")
        void shouldStartAndStopPaymentProcessingTimer() {
            Timer.Sample sample = businessMetricsService.startPaymentProcessingTimer();
            businessMetricsService.stopPaymentProcessingTimer(sample);

            Timer timer = meterRegistry.find("supplemart_payment_processing_time").timer();
            assertThat(timer).isNotNull();
            assertThat(timer.count()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Summary Tests")
    class SummaryTests {

        @Test
        @DisplayName("Should record order value")
        void shouldRecordOrderValue() {
            businessMetricsService.recordOrderValue(99.99);
            businessMetricsService.recordOrderValue(149.99);

            var summary = meterRegistry.find("supplemart_order_value").summary();
            assertThat(summary).isNotNull();
            assertThat(summary.count()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Dynamic Gauge Registration Tests")
    class DynamicGaugeTests {

        @Test
        @DisplayName("Should register dynamic gauge")
        void shouldRegisterDynamicGauge() {
            businessMetricsService.registerGauge(
                    "custom_metric",
                    "A custom metric",
                    () -> 42
            );

            Double value = meterRegistry.find("custom_metric").gauge().value();
            assertThat(value).isEqualTo(42.0);
        }
    }
}
