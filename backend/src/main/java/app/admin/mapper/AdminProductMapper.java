package app.admin.mapper;

import app.admin.dto.AdminProductDTO;
import app.admin.dto.AdminProductPageResponse;
import app.admin.dto.CreateProductRequest;
import app.admin.dto.UpdateProductRequest;
import app.product.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

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

    public AdminProductDTO toAdminDTO(Product product, Integer totalSales) {
        if (product == null) {
            return null;
        }

        return AdminProductDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .category(product.getCategory())
                .stockQuantity(product.getStockQuantity())
                .imageUrl(product.getImageUrl())
                .inStock(product.getStockQuantity() > 0)
                .isActive(product.isActive())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .totalSales(totalSales != null ? totalSales : 0)
                .build();
    }

    public List<AdminProductDTO> toAdminDTOList(List<Product> products, Map<UUID, Integer> salesMap) {
        return products.stream()
                .map(product -> toAdminDTO(product, salesMap.getOrDefault(product.getId(), 0)))
                .collect(Collectors.toList());
    }

    public AdminProductPageResponse toAdminPageResponse(Page<Product> productPage, Map<UUID, Integer> salesMap) {
        List<AdminProductDTO> productDTOs = toAdminDTOList(productPage.getContent(), salesMap);

        return AdminProductPageResponse.builder()
                .products(productDTOs)
                .currentPage(productPage.getNumber())
                .totalPages(productPage.getTotalPages())
                .totalItems(productPage.getTotalElements())
                .pageSize(productPage.getSize())
                .build();
    }
}

