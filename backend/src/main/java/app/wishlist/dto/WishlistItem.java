package app.wishlist.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WishlistItem {
    private UUID id;
    private UUID productId;
    private String productName;
    private String productDescription;
    private Double price;
    private Double originalPrice;
    private String category;
    private String imageUrl;
    private Boolean inStock;
    private Integer stockQuantity;
    private Double averageRating;
    private Integer totalReviews;
    private LocalDateTime addedAt;
}
