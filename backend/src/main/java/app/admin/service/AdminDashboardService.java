package app.admin.service;

import app.admin.dto.DashboardStats;
import app.admin.mapper.AdminMapper;
import app.order.service.OrderService;
import app.product.service.ProductService;
import app.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    public DashboardStats getDashboardStats() {
        log.info("Fetching dashboard statistics");

        Long totalProducts = productService.getTotalProductsCount();
        Long totalUsers = userService.getTotalUsersCount();
        Long totalOrders = orderService.getTotalOrdersCount();
        BigDecimal totalRevenue = orderService.getTotalRevenue();
        Long pendingOrders = orderService.getPendingOrdersCount();
        Long lowStockProducts = productService.getLowStockProductsCount();

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

