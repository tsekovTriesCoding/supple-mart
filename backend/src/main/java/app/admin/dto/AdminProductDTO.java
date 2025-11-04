package app.admin.dto;

import app.product.model.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminProductDTO {
    private UUID id;
    private String name;
    private String description;
    private BigDecimal price;
    private Category category;
    private Integer stockQuantity;
    private String imageUrl;
    private boolean inStock;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer totalSales;
}

