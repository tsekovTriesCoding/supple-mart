package app.monitoring;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import org.springframework.stereotype.Service;

import app.cart.service.CartService;
import app.order.service.OrderService;
import app.product.service.ProductService;
import app.user.service.UserService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for tracking custom business metrics.
 * Exposes application-specific metrics to Prometheus via Micrometer.
 */
@Service
@Slf4j
public class BusinessMetricsService {

    private final MeterRegistry meterRegistry;

    // Counters
    private final Counter ordersCreatedCounter;
    private final Counter userRegistrationsCounter;
    private final Counter productsViewedCounter;
    private final Counter cartOperationsCounter;
    private final Counter paymentSuccessCounter;
    private final Counter paymentFailureCounter;
    private final Counter oauth2LoginsCounter;

    // Gauges
    private final AtomicInteger activeCartsCount = new AtomicInteger(0);

    // Timers
    private final Timer orderProcessingTimer;
    private final Timer paymentProcessingTimer;

    public BusinessMetricsService(MeterRegistry meterRegistry,
                                  UserService userService,
                                  ProductService productService,
                                  OrderService orderService,
                                  CartService cartService) {
        this.meterRegistry = meterRegistry;

        // Initialize counters
        this.ordersCreatedCounter = Counter.builder("supplemart_orders_total")
                .description("Total number of orders created")
                .tag("type", "all")
                .register(meterRegistry);

        this.userRegistrationsCounter = Counter.builder("supplemart_user_registrations_total")
                .description("Total number of user registrations")
                .register(meterRegistry);

        this.productsViewedCounter = Counter.builder("supplemart_products_viewed_total")
                .description("Total number of product views")
                .register(meterRegistry);

        this.cartOperationsCounter = Counter.builder("supplemart_cart_operations_total")
                .description("Total number of cart operations")
                .register(meterRegistry);

        this.paymentSuccessCounter = Counter.builder("supplemart_payments_total")
                .description("Total number of successful payments")
                .tag("status", "success")
                .register(meterRegistry);

        this.paymentFailureCounter = Counter.builder("supplemart_payments_total")
                .description("Total number of failed payments")
                .tag("status", "failure")
                .register(meterRegistry);

        this.oauth2LoginsCounter = Counter.builder("supplemart_oauth2_logins_total")
                .description("Total number of OAuth2 logins")
                .register(meterRegistry);

        // Initialize gauges - Active carts
        Gauge.builder("supplemart_active_carts", activeCartsCount, AtomicInteger::get)
                .description("Number of active shopping carts")
                .register(meterRegistry);

        // Database metrics gauges - using services for proper abstraction
        Gauge.builder("supplemart_users", userService, UserService::getTotalUsersCount)
                .description("Total number of registered users")
                .register(meterRegistry);

        Gauge.builder("supplemart_products", productService, ProductService::countProducts)
                .description("Total number of products in catalog")
                .register(meterRegistry);

        Gauge.builder("supplemart_products_low_stock", productService, ProductService::countLowStockProducts)
                .description("Number of products with low stock (< 10)")
                .register(meterRegistry);

        Gauge.builder("supplemart_orders_count", orderService, OrderService::countOrders)
                .description("Total number of orders in database")
                .register(meterRegistry);

        Gauge.builder("supplemart_carts", cartService, CartService::countCarts)
                .description("Total number of shopping carts")
                .register(meterRegistry);

        // Initialize timers
        this.orderProcessingTimer = Timer.builder("supplemart_order_processing_time")
                .description("Time taken to process orders")
                .register(meterRegistry);

        this.paymentProcessingTimer = Timer.builder("supplemart_payment_processing_time")
                .description("Time taken to process payments")
                .register(meterRegistry);

        log.info("Business metrics service initialized");
    }

    // Counter methods
    public void incrementOrdersCreated() {
        ordersCreatedCounter.increment();
    }

    public void incrementOrdersCreated(String status) {
        Counter.builder("supplemart_orders_created_total")
                .description("Total number of orders created")
                .tag("status", status)
                .register(meterRegistry)
                .increment();
    }

    public void incrementUserRegistrations() {
        userRegistrationsCounter.increment();
    }

    public void incrementUserRegistrations(String provider) {
        Counter.builder("supplemart_user_registrations_total")
                .description("Total number of user registrations")
                .tag("provider", provider)
                .register(meterRegistry)
                .increment();
    }

    public void incrementProductViews() {
        productsViewedCounter.increment();
    }

    public void incrementProductViews(String productId, String category) {
        Counter.builder("supplemart_products_viewed_total")
                .description("Total number of product views")
                .tag("product_id", productId)
                .tag("category", category)
                .register(meterRegistry)
                .increment();
    }

    public void incrementCartOperations(String operation) {
        Counter.builder("supplemart_cart_operations_total")
                .description("Total number of cart operations")
                .tag("operation", operation) // add, remove, update, clear
                .register(meterRegistry)
                .increment();
    }

    public void incrementPaymentSuccess() {
        paymentSuccessCounter.increment();
    }

    public void incrementPaymentFailure() {
        paymentFailureCounter.increment();
    }

    public void incrementOAuth2Logins(String provider) {
        Counter.builder("supplemart_oauth2_logins_total")
                .description("Total number of OAuth2 logins")
                .tag("provider", provider)
                .register(meterRegistry)
                .increment();
    }

    // Gauge methods
    public void setActiveCartsCount(int count) {
        activeCartsCount.set(count);
    }

    public void incrementActiveCarts() {
        activeCartsCount.incrementAndGet();
    }

    public void decrementActiveCarts() {
        activeCartsCount.decrementAndGet();
    }

    // Timer methods
    public Timer.Sample startOrderProcessingTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopOrderProcessingTimer(Timer.Sample sample) {
        sample.stop(orderProcessingTimer);
    }

    public Timer.Sample startPaymentProcessingTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopPaymentProcessingTimer(Timer.Sample sample) {
        sample.stop(paymentProcessingTimer);
    }

    // Utility method for recording values
    public void recordOrderValue(double amount) {
        meterRegistry.summary("supplemart_order_value")
                .record(amount);
    }

    // Register dynamic gauges
    public void registerGauge(String name, String description, Supplier<Number> valueSupplier) {
        Gauge.builder(name, valueSupplier)
                .description(description)
                .register(meterRegistry);
    }
}
