package app.order.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class OrderItemResponse {
    private UUID id;
    private ProductInfo product;
    private Integer quantity;
    private BigDecimal price;

    @Data
    @Builder
    public static class ProductInfo {
        private UUID id;
        private String name;
        private String imageUrl;
    }
}
