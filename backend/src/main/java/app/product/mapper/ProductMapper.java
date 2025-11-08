package app.product.mapper;

import app.product.dto.ProductDTO;
import app.product.dto.ProductDetailsDTO;
import app.product.dto.ProductPageResponse;
import app.product.model.Product;
import app.review.dto.ReviewDTO;
import app.review.mapper.ReviewMapper;
import app.review.model.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", uses = {ReviewMapper.class})
public abstract class ProductMapper {

    protected ReviewMapper reviewMapper;

    public ProductMapper() {
    }

    @Autowired
    public void setReviewMapper(ReviewMapper reviewMapper) {
        this.reviewMapper = reviewMapper;
    }

    @Mapping(target = "inStock", expression = "java(isInStock(product))")
    @Mapping(target = "averageRating", expression = "java(calculateAverageRating(product))")
    @Mapping(target = "totalReviews", expression = "java(calculateTotalReviews(product))")
    public abstract ProductDTO toDTO(Product product);

    public abstract List<ProductDTO> toDTOList(List<Product> products);

    @Mapping(target = "inStock", expression = "java(isInStock(product))")
    @Mapping(target = "reviews", expression = "java(mapReviews(product))")
    @Mapping(target = "averageRating", expression = "java(calculateAverageRatingFromDTOs(mapReviews(product)))")
    @Mapping(target = "totalReviews", expression = "java(mapReviews(product).size())")
    public abstract ProductDetailsDTO toDetailsDTO(Product product);

    protected boolean isInStock(Product product) {
        return product != null && product.getStockQuantity() > 0;
    }

    protected double calculateAverageRating(Product product) {
        if (product == null || product.getReviews() == null || product.getReviews().isEmpty()) {
            return 0.0;
        }
        return product.getReviews().stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
    }

    protected int calculateTotalReviews(Product product) {
        if (product == null || product.getReviews() == null) {
            return 0;
        }
        return product.getReviews().size();
    }

    protected List<ReviewDTO> mapReviews(Product product) {
        if (product == null || product.getReviews() == null) {
            return List.of();
        }
        return reviewMapper.toDTOList(product.getReviews());
    }

    protected Double calculateAverageRatingFromDTOs(List<ReviewDTO> reviews) {
        if (reviews == null || reviews.isEmpty()) {
            return 0.0;
        }
        return reviews.stream()
                .mapToInt(ReviewDTO::getRating)
                .average()
                .orElse(0.0);
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
