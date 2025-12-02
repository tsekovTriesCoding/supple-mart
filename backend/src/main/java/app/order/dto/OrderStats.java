package app.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStats {
    private Long totalOrders;
    private Long pendingCount;
    private Long paidCount;
    private Long processingCount;
    private Long shippedCount;
    private Long deliveredCount;
    private Long cancelledCount;
    private BigDecimal totalSpent;
}
