package app.product.dto;

import app.product.model.Category;
import app.review.dto.Review;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class ProductDetails {
    private UUID id;
    private String name;
    private String description;
    private BigDecimal price;
    private String imageUrl;
    private Category category;
    private Integer stockQuantity;
    private boolean inStock;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<Review> reviews;
    private Double averageRating;
    private Integer totalReviews;
}
