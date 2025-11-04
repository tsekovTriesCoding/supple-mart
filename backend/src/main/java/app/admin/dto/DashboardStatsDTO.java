package app.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDTO {
    private Long totalProducts;
    private Long totalUsers;
    private Long totalOrders;
    private BigDecimal totalRevenue;
    private Long pendingOrders;
    private Long lowStockProducts;
}

