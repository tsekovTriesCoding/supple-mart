package app.review.service;

import app.exception.DuplicateResourceException;
import app.exception.ResourceNotFoundException;
import app.exception.UnauthorizedException;
import app.product.model.Product;
import app.product.service.ProductService;
import app.review.dto.CreateReviewRequest;
import app.review.dto.UpdateReviewRequest;
import app.review.dto.Review;
import app.review.dto.ReviewResponse;
import app.review.mapper.ReviewMapper;
import app.review.repository.ReviewRepository;
import app.user.model.User;
import app.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;
    private final UserService userService;
    private final ProductService productService;

    @Transactional(readOnly = true)
    public List<Review> getUserReviews(UUID userId) {
        List<app.review.model.Review> reviews = reviewRepository.findByUserId(userId);
        return reviewMapper.toReviewList(reviews);
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getUserReviewsDetailed(UUID userId) {
        List<app.review.model.Review> reviews = reviewRepository.findByUserId(userId);
        return reviewMapper.toReviewResponseList(reviews);
    }

    @Transactional
    public Review createReview(UUID userId, CreateReviewRequest request) {
        if (reviewRepository.existsByUserIdAndProductId(userId, request.getProductId())) {
            throw new DuplicateResourceException("You have already reviewed this product");
        }

        User user = userService.getUserById(userId);
        Product product = productService.getProductById(request.getProductId());

        app.review.model.Review review = app.review.model.Review.builder()
                .user(user)
                .product(product)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        app.review.model.Review savedReview = reviewRepository.save(review);
        return reviewMapper.toReview(savedReview);
    }

    @Transactional
    public void deleteReview(UUID reviewId, UUID userId) {
        app.review.model.Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review with ID " + reviewId + " not found"));

        if (!review.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You can only delete your own reviews");
        }

        reviewRepository.delete(review);
    }

    @Transactional
    public Review updateReview(UUID reviewId, UUID userId, UpdateReviewRequest request) {
        app.review.model.Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review with ID " + reviewId + " not found"));

        if (!review.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You can only update your own reviews");
        }

        review.setRating(request.getRating());
        review.setComment(request.getComment());

        app.review.model.Review savedReview = reviewRepository.save(review);
        return reviewMapper.toReview(savedReview);
    }
}
