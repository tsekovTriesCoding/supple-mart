package app.admin.mapper;

import app.admin.dto.CreateProductRequest;
import app.admin.dto.UpdateProductRequest;
import app.product.model.Product;
import org.springframework.stereotype.Component;

@Component
public class AdminProductMapper {

    public Product toEntity(CreateProductRequest request) {
        if (request == null) {
            return null;
        }

        return Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .imageUrl(request.getImageUrl())
                .category(request.getCategory())
                .stockQuantity(request.getStockQuantity())
                .isActive(request.getIsActive())
                .build();
    }

    public void updateEntity(Product product, UpdateProductRequest request) {
        if (product == null || request == null) {
            return;
        }

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setImageUrl(request.getImageUrl());
        product.setCategory(request.getCategory());
        product.setStockQuantity(request.getStockQuantity());
        product.setActive(request.getIsActive());
    }
}

