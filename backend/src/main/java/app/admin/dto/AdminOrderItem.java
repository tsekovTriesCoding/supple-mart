package app.admin.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class AdminOrderItem {
    private UUID id;
    private String productName;
    private Integer quantity;
    private BigDecimal price;
}
