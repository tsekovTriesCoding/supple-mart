package app.web;

import app.BaseIntegrationTest;
import app.product.model.Category;
import app.product.model.Product;
import app.product.repository.ProductRepository;
import app.testutil.TestDataFactory;
import app.user.model.AuthProvider;
import app.user.model.Role;
import app.user.model.User;
import app.wishlist.dto.AddToWishlistRequest;
import app.wishlist.model.Wishlist;
import app.wishlist.repository.WishlistRepository;
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
 * Integration tests for WishlistController.
 * Tests wishlist management endpoints with a real database using Testcontainers.
 */
@DisplayName("Wishlist Controller Integration Tests")
class WishlistControllerIntegrationTest extends BaseIntegrationTest {

    private static final String WISHLIST_BASE_URL = "/api/wishlist";

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private Product testProduct;
    private Product anotherProduct;
    private String authToken;

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

        testProduct = Product.builder()
                .name("Wishlist Test Product")
                .description("Product for wishlist testing")
                .price(new BigDecimal("49.99"))
                .category(Category.PROTEIN)
                .stockQuantity(50)
                .isActive(true)
                .build();
        testProduct = productRepository.save(testProduct);

        anotherProduct = Product.builder()
                .name("Another Wishlist Product")
                .description("Another product for wishlist testing")
                .price(new BigDecimal("29.99"))
                .category(Category.VITAMINS)
                .stockQuantity(100)
                .isActive(true)
                .build();
        anotherProduct = productRepository.save(anotherProduct);
    }

    @Nested
    @DisplayName("POST /api/wishlist")
    class AddToWishlistTests {

        @Test
        @DisplayName("Should add product to wishlist successfully")
        void addToWishlist_ValidProduct_ReturnsCreated() throws Exception {
            AddToWishlistRequest request = AddToWishlistRequest.builder()
                    .productId(testProduct.getId())
                    .build();

            mockMvc.perform(post(WISHLIST_BASE_URL)
                            .header("Authorization", bearerToken(authToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.message").value("Product added to wishlist successfully"));
        }

        @Test
        @DisplayName("Should return 409 when product already in wishlist")
        void addToWishlist_ProductAlreadyInWishlist_ReturnsConflict() throws Exception {
            Wishlist wishlist = Wishlist.builder()
                    .user(testUser)
                    .product(testProduct)
                    .build();
            wishlistRepository.save(wishlist);

            AddToWishlistRequest request = AddToWishlistRequest.builder()
                    .productId(testProduct.getId())
                    .build();

            mockMvc.perform(post(WISHLIST_BASE_URL)
                            .header("Authorization", bearerToken(authToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("Should return 404 for non-existent product")
        void addToWishlist_NonExistentProduct_ReturnsNotFound() throws Exception {
            AddToWishlistRequest request = AddToWishlistRequest.builder()
                    .productId(UUID.randomUUID())
                    .build();

            mockMvc.perform(post(WISHLIST_BASE_URL)
                            .header("Authorization", bearerToken(authToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 401 without authentication")
        void addToWishlist_WithoutAuth_ReturnsUnauthorized() throws Exception {
            AddToWishlistRequest request = AddToWishlistRequest.builder()
                    .productId(testProduct.getId())
                    .build();

            mockMvc.perform(post(WISHLIST_BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("DELETE /api/wishlist/{productId}")
    class RemoveFromWishlistTests {

        @BeforeEach
        void addProductToWishlist() {
            Wishlist wishlist = Wishlist.builder()
                    .user(testUser)
                    .product(testProduct)
                    .build();
            wishlistRepository.save(wishlist);
        }

        @Test
        @DisplayName("Should remove product from wishlist successfully")
        void removeFromWishlist_ProductInWishlist_ReturnsOk() throws Exception {
            mockMvc.perform(delete(WISHLIST_BASE_URL + "/" + testProduct.getId())
                            .header("Authorization", bearerToken(authToken))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Product removed from wishlist successfully"));
        }

        @Test
        @DisplayName("Should return 404 when product not in wishlist")
        void removeFromWishlist_ProductNotInWishlist_ReturnsNotFound() throws Exception {
            mockMvc.perform(delete(WISHLIST_BASE_URL + "/" + anotherProduct.getId())
                            .header("Authorization", bearerToken(authToken))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 401 without authentication")
        void removeFromWishlist_WithoutAuth_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(delete(WISHLIST_BASE_URL + "/" + testProduct.getId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 400 for invalid UUID format")
        void removeFromWishlist_InvalidUuidFormat_ReturnsBadRequest() throws Exception {
            mockMvc.perform(delete(WISHLIST_BASE_URL + "/invalid-uuid")
                            .header("Authorization", bearerToken(authToken))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/wishlist")
    class GetWishlistTests {

        @Test
        @DisplayName("Should return empty wishlist for new user")
        void getWishlist_NoItems_ReturnsEmptyWishlist() throws Exception {
            mockMvc.perform(get(WISHLIST_BASE_URL)
                            .header("Authorization", bearerToken(authToken))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content", hasSize(0)))
                    .andExpect(jsonPath("$.totalElements").value(0));
        }

        @Test
        @DisplayName("Should return wishlist with items")
        void getWishlist_WithItems_ReturnsWishlistItems() throws Exception {
            wishlistRepository.save(Wishlist.builder()
                    .user(testUser)
                    .product(testProduct)
                    .build());

            wishlistRepository.save(Wishlist.builder()
                    .user(testUser)
                    .product(anotherProduct)
                    .build());

            mockMvc.perform(get(WISHLIST_BASE_URL)
                            .header("Authorization", bearerToken(authToken))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content", hasSize(2)))
                    .andExpect(jsonPath("$.totalElements").value(2));
        }

        @Test
        @DisplayName("Should paginate wishlist correctly")
        void getWishlist_WithPagination_ReturnsPaginatedResults() throws Exception {
            wishlistRepository.save(Wishlist.builder()
                    .user(testUser)
                    .product(testProduct)
                    .build());

            wishlistRepository.save(Wishlist.builder()
                    .user(testUser)
                    .product(anotherProduct)
                    .build());

            mockMvc.perform(get(WISHLIST_BASE_URL)
                            .header("Authorization", bearerToken(authToken))
                            .param("page", "0")
                            .param("size", "1")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.currentPage").value(0))
                    .andExpect(jsonPath("$.totalPages").value(2));
        }

        @Test
        @DisplayName("Should return 401 without authentication")
        void getWishlist_WithoutAuth_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(get(WISHLIST_BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/wishlist/check/{productId}")
    class CheckIfInWishlistTests {

        @Test
        @DisplayName("Should return true when product is in wishlist")
        void checkIfInWishlist_ProductInWishlist_ReturnsTrue() throws Exception {
            wishlistRepository.save(Wishlist.builder()
                    .user(testUser)
                    .product(testProduct)
                    .build());

            mockMvc.perform(get(WISHLIST_BASE_URL + "/check/" + testProduct.getId())
                            .header("Authorization", bearerToken(authToken))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.inWishlist").value(true));
        }

        @Test
        @DisplayName("Should return false when product is not in wishlist")
        void checkIfInWishlist_ProductNotInWishlist_ReturnsFalse() throws Exception {
            mockMvc.perform(get(WISHLIST_BASE_URL + "/check/" + testProduct.getId())
                            .header("Authorization", bearerToken(authToken))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.inWishlist").value(false));
        }

        @Test
        @DisplayName("Should return 401 without authentication")
        void checkIfInWishlist_WithoutAuth_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(get(WISHLIST_BASE_URL + "/check/" + testProduct.getId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 400 for invalid UUID format")
        void checkIfInWishlist_InvalidUuidFormat_ReturnsBadRequest() throws Exception {
            mockMvc.perform(get(WISHLIST_BASE_URL + "/check/invalid-uuid")
                            .header("Authorization", bearerToken(authToken))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/wishlist/count")
    class GetWishlistCountTests {

        @Test
        @DisplayName("Should return 0 for empty wishlist")
        void getWishlistCount_EmptyWishlist_ReturnsZero() throws Exception {
            mockMvc.perform(get(WISHLIST_BASE_URL + "/count")
                            .header("Authorization", bearerToken(authToken))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.count").value(0));
        }

        @Test
        @DisplayName("Should return correct count for wishlist with items")
        void getWishlistCount_WithItems_ReturnsCorrectCount() throws Exception {
            wishlistRepository.save(Wishlist.builder()
                    .user(testUser)
                    .product(testProduct)
                    .build());

            wishlistRepository.save(Wishlist.builder()
                    .user(testUser)
                    .product(anotherProduct)
                    .build());

            mockMvc.perform(get(WISHLIST_BASE_URL + "/count")
                            .header("Authorization", bearerToken(authToken))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.count").value(2));
        }

        @Test
        @DisplayName("Should return 401 without authentication")
        void getWishlistCount_WithoutAuth_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(get(WISHLIST_BASE_URL + "/count")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }
}
