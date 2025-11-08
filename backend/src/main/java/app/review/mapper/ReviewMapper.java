package app.review.mapper;

import app.review.dto.ReviewDTO;
import app.review.dto.ReviewResponseDTO;
import app.review.model.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userName", expression = "java(review.getUser().getFirstName() + \" \" + review.getUser().getLastName())")
    ReviewDTO toDTO(Review review);

    List<ReviewDTO> toDTOList(List<Review> reviews);

    @Mapping(target = "user", source = "user")
    @Mapping(target = "product", source = "product")
    ReviewResponseDTO toResponseDTO(Review review);

    @Mapping(target = "name", expression = "java(user.getFirstName() + \" \" + user.getLastName())")
    ReviewResponseDTO.UserInfo toUserInfo(app.user.model.User user);

    @Mapping(target = "imageUrl", expression = "java(product.getImageUrl() != null && !product.getImageUrl().isEmpty() ? product.getImageUrl() : null)")
    ReviewResponseDTO.ProductInfo toProductInfo(app.product.model.Product product);

    List<ReviewResponseDTO> toResponseDTOList(List<Review> reviews);
}
