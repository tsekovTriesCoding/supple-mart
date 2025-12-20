package app.review.service;

import app.exception.ResourceNotFoundException;
import app.exception.UnauthorizedException;
import app.product.model.Category;
import app.product.model.Product;
import app.product.service.ProductService;
import app.review.dto.CreateReviewRequest;
import app.review.dto.Review;
import app.review.dto.ReviewResponse;
import app.review.dto.UpdateReviewRequest;
import app.review.mapper.ReviewMapper;
import app.review.repository.ReviewRepository;
import app.user.model.AuthProvider;
import app.user.model.Role;
import app.user.model.User;
import app.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewService Unit Tests")
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ReviewMapper reviewMapper;

    @Mock
    private UserService userService;

    @Mock
    private ProductService productService;

    @InjectMocks
    private ReviewService reviewService;

    private User testUser;
    private Product testProduct;
    private app.review.model.Review testReviewEntity;
    private Review testReviewDto;
    private UUID userId;
    private UUID productId;
    private UUID reviewId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        productId = UUID.randomUUID();
        reviewId = UUID.randomUUID();

        testUser = User.builder()
                .id(userId)
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .role(Role.CUSTOMER)
                .authProvider(AuthProvider.LOCAL)
                .build();

        testProduct = Product.builder()
                .id(productId)
                .name("Test Product")
                .price(new BigDecimal("29.99"))
                .category(Category.PROTEIN)
                .build();

        testReviewEntity = app.review.model.Review.builder()
                .id(reviewId)
                .user(testUser)
                .product(testProduct)
                .rating(5)
                .comment("Great product!")
                .createdAt(LocalDateTime.now())
                .build();

        testReviewDto = Review.builder()
                .id(reviewId)
                .rating(5)
                .comment("Great product!")
                .build();
    }

    @Nested
    @DisplayName("getUserReviews Tests")
    class GetUserReviewsTests {

        @Test
        @DisplayName("Should return list of user reviews")
        void getUserReviews_WithExistingReviews_ReturnsReviewList() {
            List<app.review.model.Review> reviewEntities = List.of(testReviewEntity);
            List<Review> expectedReviews = List.of(testReviewDto);

            when(reviewRepository.findByUserId(userId)).thenReturn(reviewEntities);
            when(reviewMapper.toReviewList(reviewEntities)).thenReturn(expectedReviews);

            List<Review> result = reviewService.getUserReviews(userId);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getRating()).isEqualTo(5);
            verify(reviewRepository).findByUserId(userId);
        }

        @Test
        @DisplayName("Should return empty list when no reviews exist")
        void getUserReviews_WithNoReviews_ReturnsEmptyList() {
            when(reviewRepository.findByUserId(userId)).thenReturn(List.of());
            when(reviewMapper.toReviewList(List.of())).thenReturn(List.of());

            List<Review> result = reviewService.getUserReviews(userId);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getUserReviewsDetailed Tests")
    class GetUserReviewsDetailedTests {

        @Test
        @DisplayName("Should return detailed review responses")
        void getUserReviewsDetailed_WithExistingReviews_ReturnsDetailedResponses() {
            List<app.review.model.Review> reviewEntities = List.of(testReviewEntity);
            ReviewResponse detailedResponse = ReviewResponse.builder().build();
            List<ReviewResponse> expectedResponses = List.of(detailedResponse);

            when(reviewRepository.findByUserId(userId)).thenReturn(reviewEntities);
            when(reviewMapper.toReviewResponseList(reviewEntities)).thenReturn(expectedResponses);

            List<ReviewResponse> result = reviewService.getUserReviewsDetailed(userId);

            assertThat(result).hasSize(1);
            verify(reviewMapper).toReviewResponseList(reviewEntities);
        }
    }

    @Nested
    @DisplayName("createReview Tests")
    class CreateReviewTests {

        @Test
        @DisplayName("Should create review successfully")
        void createReview_WithValidRequest_ReturnsCreatedReview() {
            CreateReviewRequest request = new CreateReviewRequest();
            request.setProductId(productId);
            request.setRating(5);
            request.setComment("Excellent product!");

            when(userService.getUserById(userId)).thenReturn(testUser);
            when(productService.getProductById(productId)).thenReturn(testProduct);
            when(reviewRepository.save(any(app.review.model.Review.class))).thenReturn(testReviewEntity);
            when(reviewMapper.toReview(testReviewEntity)).thenReturn(testReviewDto);

            Review result = reviewService.createReview(userId, request);

            assertThat(result).isNotNull();
            assertThat(result.getRating()).isEqualTo(5);
            verify(reviewRepository).save(any(app.review.model.Review.class));
        }
    }

    @Nested
    @DisplayName("updateReview Tests")
    class UpdateReviewTests {

        @Test
        @DisplayName("Should update review successfully when user is owner")
        void updateReview_WithValidOwner_ReturnsUpdatedReview() {
            UpdateReviewRequest request = new UpdateReviewRequest();
            request.setRating(4);
            request.setComment("Updated comment");

            when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(testReviewEntity));
            when(reviewRepository.save(testReviewEntity)).thenReturn(testReviewEntity);
            when(reviewMapper.toReview(testReviewEntity)).thenReturn(testReviewDto);

            Review result = reviewService.updateReview(reviewId, userId, request);

            assertThat(result).isNotNull();
            assertThat(testReviewEntity.getRating()).isEqualTo(4);
            assertThat(testReviewEntity.getComment()).isEqualTo("Updated comment");
            verify(reviewRepository).save(testReviewEntity);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when review not found")
        void updateReview_WithNonExistentReview_ThrowsException() {
            UUID nonExistentId = UUID.randomUUID();
            UpdateReviewRequest request = new UpdateReviewRequest();

            when(reviewRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> reviewService.updateReview(nonExistentId, userId, request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining(nonExistentId.toString());
        }

        @Test
        @DisplayName("Should throw UnauthorizedException when user is not owner")
        void updateReview_WithNonOwner_ThrowsException() {
            UUID differentUserId = UUID.randomUUID();
            UpdateReviewRequest request = new UpdateReviewRequest();

            when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(testReviewEntity));

            assertThatThrownBy(() -> reviewService.updateReview(reviewId, differentUserId, request))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("only update your own reviews");
        }
    }

    @Nested
    @DisplayName("deleteReview Tests")
    class DeleteReviewTests {

        @Test
        @DisplayName("Should delete review successfully when user is owner")
        void deleteReview_WithValidOwner_DeletesReview() {
            when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(testReviewEntity));

            reviewService.deleteReview(reviewId, userId);

            verify(reviewRepository).delete(testReviewEntity);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when review not found")
        void deleteReview_WithNonExistentReview_ThrowsException() {
            UUID nonExistentId = UUID.randomUUID();
            when(reviewRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> reviewService.deleteReview(nonExistentId, userId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw UnauthorizedException when user is not owner")
        void deleteReview_WithNonOwner_ThrowsException() {
            UUID differentUserId = UUID.randomUUID();
            when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(testReviewEntity));

            assertThatThrownBy(() -> reviewService.deleteReview(reviewId, differentUserId))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("only delete your own reviews");
        }
    }
}
