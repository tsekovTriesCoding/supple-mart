package app.review.service;

import app.product.model.Product;
import app.product.service.ProductService;
import app.review.dto.CreateReviewRequest;
import app.review.dto.ReviewDTO;
import app.review.dto.ReviewResponseDTO;
import app.review.mapper.ReviewMapper;
import app.review.model.Review;
import app.review.repository.ReviewRepository;
import app.user.model.User;
import app.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;
    private final UserService userService;
    private final ProductService productService;

    public List<ReviewDTO> getUserReviews(UUID userId) {
        List<Review> reviews = reviewRepository.findByUserId(userId);
        return reviewMapper.toDTOList(reviews);
    }

    public List<ReviewResponseDTO> getUserReviewsDetailed(UUID userId) {
        List<Review> reviews = reviewRepository.findByUserId(userId);
        return reviewMapper.toResponseDTOList(reviews);
    }

    public ReviewDTO createReview(UUID userId, CreateReviewRequest request) {
        User user = userService.getUserById(userId);
        Product product = productService.getProductById(request.getProductId());

        Review review = Review.builder()
                .user(user)
                .product(product)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        Review savedReview = reviewRepository.save(review);
        return reviewMapper.toDTO(savedReview);
    }

}
