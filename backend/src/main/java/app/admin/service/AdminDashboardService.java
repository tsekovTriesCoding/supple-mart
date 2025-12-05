package app.admin.service;

import app.admin.dto.DashboardStats;
import app.admin.mapper.AdminMapper;
import app.config.CacheConfig;
import app.order.service.OrderService;
import app.product.service.ProductService;
import app.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminDashboardService {

    private final ProductService productService;
    private final UserService userService;
    private final OrderService orderService;
    private final AdminMapper adminMapper;

    /**
     * Get dashboard statistics - cached to reduce database load.
     * This is an expensive operation that aggregates data from multiple tables.
     * Cache expires every 5 minutes to balance freshness vs performance.
     */
    @Cacheable(value = CacheConfig.DASHBOARD_STATS_CACHE, key = "'stats'")
    public DashboardStats getDashboardStats() {
        log.info("Fetching dashboard statistics (cache miss)");

        Long totalProducts = productService.countProducts();
        Long totalUsers = userService.getTotalUsersCount();
        Long totalOrders = orderService.countOrders();
        BigDecimal totalRevenue = orderService.getTotalRevenue();
        Long pendingOrders = orderService.countPendingOrders();
        Long lowStockProducts = productService.countLowStockProducts();

        return adminMapper.toDashboardStats(
                totalProducts,
                totalUsers,
                totalOrders,
                totalRevenue,
                pendingOrders,
                lowStockProducts
        );
    }
}

