package app.web;

import app.BaseIntegrationTest;
import app.product.model.Category;
import app.product.model.Product;
import app.product.repository.ProductRepository;
import app.review.dto.CreateReviewRequest;
import app.review.dto.UpdateReviewRequest;
import app.review.model.Review;
import app.review.repository.ReviewRepository;
import app.testutil.TestDataFactory;
import app.user.model.AuthProvider;
import app.user.model.Role;
import app.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for ReviewController.
 * Tests product review management endpoints with a real database using Testcontainers.
 */
@DisplayName("Review Controller Integration Tests")
class ReviewControllerIntegrationTest extends BaseIntegrationTest {

    private static final String REVIEWS_BASE_URL = "/api/reviews";

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private User anotherUser;
    private Product testProduct;
    private String authToken;
    private String anotherUserToken;

    @BeforeEach
    void setUp() {
        String uniqueEmail = TestDataFactory.generateUniqueEmail();
        testUser = User.builder()
                .email(uniqueEmail)
                .password(passwordEncoder.encode("Password123!"))
                .firstName("Test")
                .lastName("User")
                .role(Role.CUSTOMER)
                .authProvider(AuthProvider.LOCAL)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        testUser = userRepository.save(testUser);
        authToken = generateToken(testUser);

        String anotherEmail = TestDataFactory.generateUniqueEmail();
        anotherUser = User.builder()
                .email(anotherEmail)
                .password(passwordEncoder.encode("Password123!"))
                .firstName("Another")
                .lastName("User")
                .role(Role.CUSTOMER)
                .authProvider(AuthProvider.LOCAL)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        anotherUser = userRepository.save(anotherUser);
        anotherUserToken = generateToken(anotherUser);

        // Create test product
        testProduct = Product.builder()
                .name("Review Test Product")
                .description("Product for review testing")
                .price(new BigDecimal("39.99"))
                .category(Category.PROTEIN)
                .stockQuantity(50)
                .isActive(true)
                .build();
        testProduct = productRepository.save(testProduct);
    }

    @Nested
    @DisplayName("GET /api/reviews")
    class GetUserReviewsTests {

        @Test
        @DisplayName("Should return empty list for user with no reviews")
        void getUserReviews_NoReviews_ReturnsEmptyList() throws Exception {
            mockMvc.perform(get(REVIEWS_BASE_URL)
                            .header("Authorization", bearerToken(authToken))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        @DisplayName("Should return user's reviews")
        void getUserReviews_WithReviews_ReturnsUserReviews() throws Exception {
            Review review = Review.builder()
                    .user(testUser)
                    .product(testProduct)
                    .rating(5)
                    .comment("Excellent product! Highly recommend.")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            reviewRepository.save(review);

            mockMvc.perform(get(REVIEWS_BASE_URL)
                            .header("Authorization", bearerToken(authToken))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].rating").value(5))
                    .andExpect(jsonPath("$[0].comment").value("Excellent product! Highly recommend."));
        }

        @Test
        @DisplayName("Should return 401 without authentication")
        void getUserReviews_WithoutAuth_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(get(REVIEWS_BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("POST /api/reviews")
    class CreateReviewTests {

        @Test
        @DisplayName("Should create review successfully")
        void createReview_WithValidData_ReturnsCreated() throws Exception {
            CreateReviewRequest request = new CreateReviewRequest();
            request.setProductId(testProduct.getId());
            request.setRating(5);
            request.setComment("Great product! Really helped my workout routine.");

            mockMvc.perform(post(REVIEWS_BASE_URL)
                            .header("Authorization", bearerToken(authToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.rating").value(5))
                    .andExpect(jsonPath("$.comment").value("Great product! Really helped my workout routine."))
                    .andExpect(jsonPath("$.productId").value(testProduct.getId().toString()));
        }

        @Test
        @DisplayName("Should create review without comment")
        void createReview_WithoutComment_ReturnsCreated() throws Exception {
            CreateReviewRequest request = new CreateReviewRequest();
            request.setProductId(testProduct.getId());
            request.setRating(4);

            mockMvc.perform(post(REVIEWS_BASE_URL)
                            .header("Authorization", bearerToken(authToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.rating").value(4));
        }

        @Test
        @DisplayName("Should return 400 for invalid rating (too low)")
        void createReview_RatingTooLow_ReturnsBadRequest() throws Exception {
            CreateReviewRequest request = new CreateReviewRequest();
            request.setProductId(testProduct.getId());
            request.setRating(0);
            request.setComment("This rating should not be accepted");

            mockMvc.perform(post(REVIEWS_BASE_URL)
                            .header("Authorization", bearerToken(authToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 for invalid rating (too high)")
        void createReview_RatingTooHigh_ReturnsBadRequest() throws Exception {
            CreateReviewRequest request = new CreateReviewRequest();
            request.setProductId(testProduct.getId());
            request.setRating(6);
            request.setComment("This rating should not be accepted");

            mockMvc.perform(post(REVIEWS_BASE_URL)
                            .header("Authorization", bearerToken(authToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 for missing product ID")
        void createReview_MissingProductId_ReturnsBadRequest() throws Exception {
            CreateReviewRequest request = new CreateReviewRequest();
            request.setRating(5);
            request.setComment("Good product but product ID is missing");

            mockMvc.perform(post(REVIEWS_BASE_URL)
                            .header("Authorization", bearerToken(authToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 for missing rating")
        void createReview_MissingRating_ReturnsBadRequest() throws Exception {
            CreateReviewRequest request = new CreateReviewRequest();
            request.setProductId(testProduct.getId());
            request.setComment("Good product but rating is missing");

            mockMvc.perform(post(REVIEWS_BASE_URL)
                            .header("Authorization", bearerToken(authToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 for comment too short")
        void createReview_CommentTooShort_ReturnsBadRequest() throws Exception {
            CreateReviewRequest request = new CreateReviewRequest();
            request.setProductId(testProduct.getId());
            request.setRating(5);
            request.setComment("Short"); // Less than 10 characters

            mockMvc.perform(post(REVIEWS_BASE_URL)
                            .header("Authorization", bearerToken(authToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 404 for non-existent product")
        void createReview_NonExistentProduct_ReturnsNotFound() throws Exception {
            CreateReviewRequest request = new CreateReviewRequest();
            request.setProductId(UUID.randomUUID());
            request.setRating(5);
            request.setComment("This review is for a product that doesn't exist");

            mockMvc.perform(post(REVIEWS_BASE_URL)
                            .header("Authorization", bearerToken(authToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 401 without authentication")
        void createReview_WithoutAuth_ReturnsUnauthorized() throws Exception {
            CreateReviewRequest request = new CreateReviewRequest();
            request.setProductId(testProduct.getId());
            request.setRating(5);
            request.setComment("This review should not be created");

            mockMvc.perform(post(REVIEWS_BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("PUT /api/reviews/{id}")
    class UpdateReviewTests {

        private Review existingReview;

        @BeforeEach
        void createExistingReview() {
            existingReview = Review.builder()
                    .user(testUser)
                    .product(testProduct)
                    .rating(4)
                    .comment("Original comment for update test")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            existingReview = reviewRepository.save(existingReview);
        }

        @Test
        @DisplayName("Should update review successfully")
        void updateReview_WithValidData_ReturnsUpdatedReview() throws Exception {
            UpdateReviewRequest request = new UpdateReviewRequest();
            request.setRating(5);
            request.setComment("Updated comment - this product is even better than I thought!");

            mockMvc.perform(put(REVIEWS_BASE_URL + "/" + existingReview.getId())
                            .header("Authorization", bearerToken(authToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.rating").value(5))
                    .andExpect(jsonPath("$.comment").value("Updated comment - this product is even better than I thought!"));
        }

        @Test
        @DisplayName("Should update only rating")
        void updateReview_OnlyRating_ReturnsUpdatedReview() throws Exception {
            UpdateReviewRequest request = new UpdateReviewRequest();
            request.setRating(3);

            mockMvc.perform(put(REVIEWS_BASE_URL + "/" + existingReview.getId())
                            .header("Authorization", bearerToken(authToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.rating").value(3));
        }

        @Test
        @DisplayName("Should return 403 when updating another user's review")
        void updateReview_AnotherUsersReview_ReturnsForbidden() throws Exception {
            UpdateReviewRequest request = new UpdateReviewRequest();
            request.setRating(1);
            request.setComment("Trying to change someone else's review!");

            mockMvc.perform(put(REVIEWS_BASE_URL + "/" + existingReview.getId())
                            .header("Authorization", bearerToken(anotherUserToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 404 for non-existent review")
        void updateReview_NonExistentReview_ReturnsNotFound() throws Exception {
            UpdateReviewRequest request = new UpdateReviewRequest();
            request.setRating(5);
            request.setComment("Updating a review that doesn't exist");

            mockMvc.perform(put(REVIEWS_BASE_URL + "/" + UUID.randomUUID())
                            .header("Authorization", bearerToken(authToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 400 for invalid rating")
        void updateReview_InvalidRating_ReturnsBadRequest() throws Exception {
            UpdateReviewRequest request = new UpdateReviewRequest();
            request.setRating(10);
            request.setComment("Rating is too high");

            mockMvc.perform(put(REVIEWS_BASE_URL + "/" + existingReview.getId())
                            .header("Authorization", bearerToken(authToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 401 without authentication")
        void updateReview_WithoutAuth_ReturnsUnauthorized() throws Exception {
            UpdateReviewRequest request = new UpdateReviewRequest();
            request.setRating(5);

            mockMvc.perform(put(REVIEWS_BASE_URL + "/" + existingReview.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("DELETE /api/reviews/{id}")
    class DeleteReviewTests {

        private Review existingReview;

        @BeforeEach
        void createExistingReview() {
            existingReview = Review.builder()
                    .user(testUser)
                    .product(testProduct)
                    .rating(3)
                    .comment("Review that will be deleted")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            existingReview = reviewRepository.save(existingReview);
        }

        @Test
        @DisplayName("Should delete review successfully")
        void deleteReview_OwnReview_ReturnsNoContent() throws Exception {
            mockMvc.perform(delete(REVIEWS_BASE_URL + "/" + existingReview.getId())
                            .header("Authorization", bearerToken(authToken))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent());

            // Verify review is deleted
            mockMvc.perform(get(REVIEWS_BASE_URL)
                            .header("Authorization", bearerToken(authToken))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        @DisplayName("Should return 403 when deleting another user's review")
        void deleteReview_AnotherUsersReview_ReturnsForbidden() throws Exception {
            mockMvc.perform(delete(REVIEWS_BASE_URL + "/" + existingReview.getId())
                            .header("Authorization", bearerToken(anotherUserToken))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 404 for non-existent review")
        void deleteReview_NonExistentReview_ReturnsNotFound() throws Exception {
            mockMvc.perform(delete(REVIEWS_BASE_URL + "/" + UUID.randomUUID())
                            .header("Authorization", bearerToken(authToken))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 401 without authentication")
        void deleteReview_WithoutAuth_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(delete(REVIEWS_BASE_URL + "/" + existingReview.getId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 400 for invalid UUID format")
        void deleteReview_InvalidUuidFormat_ReturnsBadRequest() throws Exception {
            mockMvc.perform(delete(REVIEWS_BASE_URL + "/invalid-uuid")
                            .header("Authorization", bearerToken(authToken))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }
    }
}
