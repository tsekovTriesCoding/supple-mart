package app.scheduling;

import app.cart.model.Cart;
import app.cart.service.CartService;
import app.notification.event.AbandonedCartEvent;
import app.notification.event.DailyReportEvent;
import app.notification.event.LowStockAlertEvent;
import app.notification.event.ReviewReminderEvent;
import app.order.model.Order;
import app.order.model.OrderStatus;
import app.order.service.OrderService;
import app.product.model.Product;
import app.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service containing scheduled tasks for e-commerce operations.
 * Publishes events that are handled by NotificationEventListener.
 * 
 * Note: Tasks run on ThreadPoolTaskScheduler (pool size configured in SchedulingConfig).
 * No @Async needed - the scheduler thread pool handles concurrent execution.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ScheduledTasksService {

    private final CartService cartService;
    private final OrderService orderService;
    private final ProductService productService;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${scheduling.abandoned-cart.hours:24}")
    private int abandonedCartHours;

    @Value("${scheduling.auto-deliver.days:7}")
    private int autoDeliverDays;

    @Value("${scheduling.review-reminder.min-days:3}")
    private int reviewReminderMinDays;

    @Value("${scheduling.review-reminder.max-days:14}")
    private int reviewReminderMaxDays;

    @Value("${scheduling.low-stock.threshold:10}")
    private int lowStockThreshold;

    /**
     * Abandoned Cart Reminder Job
     * Runs daily at 10:00 AM to find carts that haven't been updated in X hours
     * and publishes events for notification.
     */
    @Scheduled(cron = "${scheduling.abandoned-cart.cron:0 0 10 * * ?}")
    public void processAbandonedCarts() {
        log.info("Starting abandoned cart processing job");

        LocalDateTime cutoffDate = LocalDateTime.now().minusHours(abandonedCartHours);
        List<Cart> abandonedCarts = cartService.findAbandonedCarts(cutoffDate);

        log.info("Found {} abandoned carts", abandonedCarts.size());

        for (Cart cart : abandonedCarts) {
            try {
                List<AbandonedCartEvent.CartItemData> items = cart.getItems().stream()
                        .map(item -> new AbandonedCartEvent.CartItemData(
                                item.getProduct().getName(),
                                item.getQuantity(),
                                item.getProduct().getPrice()))
                        .collect(Collectors.toList());

                BigDecimal cartTotal = cart.getItems().stream()
                        .map(item -> item.getProduct().getPrice()
                                .multiply(BigDecimal.valueOf(item.getQuantity())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                eventPublisher.publishEvent(new AbandonedCartEvent(
                        this,
                        cart.getUser().getId(),
                        cart.getUser().getEmail(),
                        cart.getUser().getFirstName(),
                        items,
                        cartTotal
                ));

                log.debug("Published AbandonedCartEvent for cart ID: {}", cart.getId());
            } catch (Exception e) {
                log.error("Failed to publish AbandonedCartEvent for cart ID: {}", cart.getId(), e);
            }
        }

        log.info("Completed abandoned cart processing job. Processed {} carts", abandonedCarts.size());
    }

    /**
     * Low Stock Alert Job
     * Runs daily at 8:00 AM to check for products with low stock
     * and publishes event for admin notification.
     */
    @Scheduled(cron = "${scheduling.low-stock.cron:0 0 8 * * ?}")
    public void checkLowStockProducts() {
        log.info("Starting low stock check job");

        List<Product> lowStockProducts = productService.findLowStockProducts(lowStockThreshold);
        List<Product> outOfStockProducts = productService.findOutOfStockProducts();

        log.info("Found {} low stock products and {} out of stock products",
                lowStockProducts.size(), outOfStockProducts.size());

        if (!lowStockProducts.isEmpty() || !outOfStockProducts.isEmpty()) {
            try {
                List<LowStockAlertEvent.ProductStockData> lowStockData = lowStockProducts.stream()
                        .map(p -> new LowStockAlertEvent.ProductStockData(p.getName(), p.getStockQuantity()))
                        .collect(Collectors.toList());

                List<LowStockAlertEvent.ProductStockData> outOfStockData = outOfStockProducts.stream()
                        .map(p -> new LowStockAlertEvent.ProductStockData(p.getName(), 0))
                        .collect(Collectors.toList());

                eventPublisher.publishEvent(new LowStockAlertEvent(this, lowStockData, outOfStockData));
                log.info("Published LowStockAlertEvent");
            } catch (Exception e) {
                log.error("Failed to publish LowStockAlertEvent", e);
            }
        }

        log.info("Completed low stock check job");
    }

    /**
     * Auto-Deliver Order Job
     * Runs daily at midnight to automatically mark shipped orders as delivered
     * after a certain number of days (simulating delivery completion).
     * Note: OrderService.autoDeliverOrder already publishes OrderDeliveredEvent.
     */
    @Scheduled(cron = "${scheduling.auto-deliver.cron:0 0 0 * * ?}")
    public void autoDeliverShippedOrders() {
        log.info("Starting auto-deliver order job");

        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(autoDeliverDays);
        List<Order> shippedOrders = orderService.findOrdersByStatusAndUpdatedBefore(
                OrderStatus.SHIPPED, cutoffDate);

        log.info("Found {} shipped orders to auto-deliver", shippedOrders.size());

        int deliveredCount = 0;
        for (Order order : shippedOrders) {
            try {
                // autoDeliverOrder publishes OrderDeliveredEvent internally
                orderService.autoDeliverOrder(order);
                deliveredCount++;
                log.debug("Auto-delivered order ID: {}", order.getId());
            } catch (Exception e) {
                log.error("Failed to auto-deliver order ID: {}", order.getId(), e);
            }
        }

        log.info("Completed auto-deliver job. Delivered {} orders", deliveredCount);
    }

    /**
     * Review Reminder Job
     * Runs daily at 2:00 PM to send review reminders for delivered orders
     * that haven't been reviewed within a certain timeframe.
     */
    @Scheduled(cron = "${scheduling.review-reminder.cron:0 0 14 * * ?}")
    public void sendReviewReminders() {
        log.info("Starting review reminder job");

        LocalDateTime startDate = LocalDateTime.now().minusDays(reviewReminderMaxDays);
        LocalDateTime endDate = LocalDateTime.now().minusDays(reviewReminderMinDays);

        List<Order> ordersWithoutReviews = orderService.findDeliveredOrdersWithoutReviews(
                startDate, endDate);

        log.info("Found {} delivered orders without reviews", ordersWithoutReviews.size());

        for (Order order : ordersWithoutReviews) {
            try {
                eventPublisher.publishEvent(new ReviewReminderEvent(
                        this,
                        order.getOrderNumber(),
                        order.getUser().getId(),
                        order.getUser().getEmail(),
                        order.getUser().getFirstName()
                ));
                log.debug("Published ReviewReminderEvent for order ID: {}", order.getId());
            } catch (Exception e) {
                log.error("Failed to publish ReviewReminderEvent for order ID: {}", order.getId(), e);
            }
        }

        log.info("Completed review reminder job. Published {} events", ordersWithoutReviews.size());
    }

    /**
     * Daily Statistics Report Job
     * Runs daily at 6:00 AM to generate and publish daily business statistics.
     */
    @Scheduled(cron = "${scheduling.daily-report.cron:0 0 6 * * ?}")
    public void generateDailyReport() {
        log.info("Starting daily report generation job");

        try {
            long totalOrders = orderService.countOrders();
            long pendingOrders = orderService.countPendingOrders();
            long lowStockCount = productService.countLowStockProducts();

            eventPublisher.publishEvent(new DailyReportEvent(
                    this, totalOrders, pendingOrders, lowStockCount));
            log.info("Published DailyReportEvent");
        } catch (Exception e) {
            log.error("Failed to publish DailyReportEvent", e);
        }

        log.info("Completed daily report generation job");
    }

    /**
     * Session Cleanup Job
     * Runs every hour to clean up expired sessions and temporary data.
     */
    @Scheduled(fixedRateString = "${scheduling.cleanup.rate:3600000}")
    public void cleanupExpiredSessions() {
        log.info("Starting session cleanup job");

        // In a real application, this would clean up:
        // - Expired JWT tokens from a blacklist
        // - Temporary uploaded files
        // - Expired password reset tokens
        // - Old notification records

        log.info("Completed session cleanup job");
    }

    /**
     * Health Check Job
     * Runs every 5 minutes to perform basic health checks.
     */
    @Scheduled(fixedDelayString = "${scheduling.health-check.delay:300000}")
    public void performHealthCheck() {
        log.debug("Performing scheduled health check");

        try {
            long productCount = productService.countProducts();
            log.debug("Health check passed. Product count: {}", productCount);
        } catch (Exception e) {
            log.error("Health check failed!", e);
        }
    }
}
