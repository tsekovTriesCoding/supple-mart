package app.product.mapper;

import app.product.dto.ProductSummary;
import app.product.dto.ProductDetails;
import app.product.dto.ProductPageResponse;
import app.product.model.Product;
import app.review.dto.Review;
import app.review.mapper.ReviewMapper;
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
    public abstract ProductSummary toProductSummary(Product product);

    public abstract List<ProductSummary> toProductSummaryList(List<Product> products);

    @Mapping(target = "inStock", expression = "java(isInStock(product))")
    @Mapping(target = "reviews", expression = "java(mapReviews(product))")
    @Mapping(target = "averageRating", expression = "java(calculateAverageRatingFromReviews(mapReviews(product)))")
    @Mapping(target = "totalReviews", expression = "java(mapReviews(product).size())")
    public abstract ProductDetails toProductDetails(Product product);

    protected boolean isInStock(Product product) {
        return product != null && product.getStockQuantity() > 0;
    }

    protected double calculateAverageRating(Product product) {
        if (product == null || product.getReviews() == null || product.getReviews().isEmpty()) {
            return 0.0;
        }
        return product.getReviews().stream()
                .mapToInt(app.review.model.Review::getRating)
                .average()
                .orElse(0.0);
    }

    protected int calculateTotalReviews(Product product) {
        if (product == null || product.getReviews() == null) {
            return 0;
        }
        return product.getReviews().size();
    }

    protected List<Review> mapReviews(Product product) {
        if (product == null || product.getReviews() == null) {
            return List.of();
        }
        return reviewMapper.toReviewList(product.getReviews());
    }

    protected Double calculateAverageRatingFromReviews(List<Review> reviews) {
        if (reviews == null || reviews.isEmpty()) {
            return 0.0;
        }
        return reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
    }

    public ProductPageResponse toPageResponse(Page<Product> productPage) {
        List<ProductSummary> products = toProductSummaryList(productPage.getContent());

        return ProductPageResponse.builder()
                .products(products)
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
