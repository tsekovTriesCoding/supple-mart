package app.monitoring;

import app.product.service.ProductService;
import app.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SuppleMartHealthIndicator implements HealthIndicator {

    private final ProductService productService;
    private final UserService userService;

    @Override
    public Health health() {
        try {
            long productCount = productService.countProducts();
            long userCount = userService.getTotalUsersCount();
            long lowStockCount = productService.countLowStockProducts();

            Health.Builder builder = Health.up()
                    .withDetail("productCount", productCount)
                    .withDetail("userCount", userCount)
                    .withDetail("lowStockProducts", lowStockCount);

            // Add warning if low stock products exist
            if (lowStockCount > 0) {
                builder.withDetail("warning", lowStockCount + " products have low stock (< 10)");
            }

            return builder.build();

        } catch (Exception e) {
            log.error("Health check failed", e);
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
