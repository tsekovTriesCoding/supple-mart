package app.review.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ReviewResponseDTO {
    private UUID id;
    private UserInfo user;
    private ProductInfo product;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    public static class UserInfo {
        private UUID id;
        private String name;
        private String email;
    }

    @Data
    @Builder
    public static class ProductInfo {
        private UUID id;
        private String name;
        private String imageUrl;
        private BigDecimal price;
    }
}
