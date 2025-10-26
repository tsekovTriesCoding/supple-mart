package app.product.mapper;

import app.product.dto.ProductDTO;
import app.product.dto.ProductDetailsDTO;
import app.product.dto.ProductPageResponse;
import app.product.model.Product;
import app.review.dto.ReviewDTO;
import app.review.mapper.ReviewMapper;
import app.review.model.Review;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProductMapper {

    private final ReviewMapper reviewMapper;

    public ProductDTO toDTO(Product product) {
        if (product == null) {
            return null;
        }

        double averageRating = 0.0;
        int totalReviews = 0;

        if (product.getReviews() != null && !product.getReviews().isEmpty()) {
            totalReviews = product.getReviews().size();
            averageRating = product.getReviews().stream()
                    .mapToInt(Review::getRating)
                    .average()
                    .orElse(0.0);
        }

        return ProductDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .imageUrl(product.getImageUrl())
                .category(product.getCategory())
                .stockQuantity(product.getStockQuantity())
                .inStock(isInStock(product))
                .averageRating(averageRating)
                .totalReviews(totalReviews)
                .build();
    }

    public List<ProductDTO> toDTOList(List<Product> products) {
        return products.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public ProductDetailsDTO toDetailsDTO(Product product) {
        if (product == null) {
            return null;
        }

        List<ReviewDTO> reviews = product.getReviews() != null
                ? reviewMapper.toDTOList(product.getReviews())
                : List.of();

        Double averageRating = reviews.isEmpty()
                ? 0.0
                : reviews.stream()
                    .mapToInt(ReviewDTO::getRating)
                    .average()
                    .orElse(0.0);

        return ProductDetailsDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .imageUrl(product.getImageUrl())
                .category(product.getCategory())
                .stockQuantity(product.getStockQuantity())
                .inStock(isInStock(product))
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .reviews(reviews)
                .averageRating(averageRating)
                .totalReviews(reviews.size())
                .build();
    }

    private boolean isInStock(Product product) {
        return product.getStockQuantity() > 0;
    }

    public ProductPageResponse toPageResponse(Page<Product> productPage) {
        List<ProductDTO> productDTOs = toDTOList(productPage.getContent());

        return ProductPageResponse.builder()
                .products(productDTOs)
                .currentPage(productPage.getNumber())
                .totalPages(productPage.getTotalPages())
                .totalElements(productPage.getTotalElements())
                .size(productPage.getSize())
                .first(productPage.isFirst())
                .last(productPage.isLast())
                .hasNext(productPage.hasNext())
                .hasPrevious(productPage.hasPrevious())
                .build();
    }
}
