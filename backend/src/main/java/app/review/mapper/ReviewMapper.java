package app.review.mapper;

import app.product.model.Product;
import app.review.dto.Review;
import app.review.dto.ReviewResponse;
import app.user.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userName", expression = "java(review.getUser().getFirstName() + \" \" + review.getUser().getLastName())")
    Review toReview(app.review.model.Review review);

    List<Review> toReviewList(List<app.review.model.Review> reviews);

    @Mapping(target = "user", source = "user")
    @Mapping(target = "product", source = "product")
    ReviewResponse toReviewResponse(app.review.model.Review review);

    @Mapping(target = "name", expression = "java(user.getFirstName() + \" \" + user.getLastName())")
    ReviewResponse.UserInfo toUserInfo(User user);

    @Mapping(target = "imageUrl", expression = "java(product.getImageUrl() != null && !product.getImageUrl().isEmpty() ? product.getImageUrl() : null)")
    ReviewResponse.ProductInfo toProductInfo(Product product);

    List<ReviewResponse> toReviewResponseList(List<app.review.model.Review> reviews);
}
