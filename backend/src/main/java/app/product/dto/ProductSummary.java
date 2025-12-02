package app.product.dto;

import app.product.model.Category;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class ProductSummary {
    private UUID id;
    private String name;
    private BigDecimal price;
    private String imageUrl;
    private Category category;
    private Integer stockQuantity;
    private boolean inStock;
    private Double averageRating;
    private Integer totalReviews;
}
