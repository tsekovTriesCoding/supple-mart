package app.web;

import app.BaseIntegrationTest;
import app.cart.dto.AddCartItemRequest;
import app.cartitem.dto.UpdateCartItemRequest;
import app.product.model.Category;
import app.product.model.Product;
import app.product.repository.ProductRepository;
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
 * Integration tests for CartController.
 * Tests shopping cart management endpoints with a real database using Testcontainers.
 */
@DisplayName("Cart Controller Integration Tests")
class CartControllerIntegrationTest extends BaseIntegrationTest {

    private static final String CART_BASE_URL = "/api/cart";

    @Autowired
    private ProductRepository productRepository;


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

        // Create test products
        testProduct = Product.builder()
                .name("Test Cart Product")
                .description("Product for cart testing")
                .price(new BigDecimal("39.99"))
                .category(Category.PROTEIN)
                .stockQuantity(50)
                .isActive(true)
                .build();
        testProduct = productRepository.save(testProduct);

        anotherProduct = Product.builder()
                .name("Another Cart Product")
                .description("Another product for cart testing")
                .price(new BigDecimal("24.99"))
                .category(Category.VITAMINS)
                .stockQuantity(100)
                .isActive(true)
                .build();
        anotherProduct = productRepository.save(anotherProduct);
    }

    @Nested
    @DisplayName("GET /api/cart")
    class GetCartTests {

        @Test
        @DisplayName("Should return empty cart for new user")
        void getCart_ForNewUser_ReturnsEmptyCart() throws Exception {
            mockMvc.perform(get(CART_BASE_URL)
                            .header("Authorization", bearerToken(authToken))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items").isArray())
                    .andExpect(jsonPath("$.totalAmount").value(0))
                    .andExpect(jsonPath("$.totalItems").value(0));
        }

        @Test
        @DisplayName("Should return cart with items")
        void getCart_WithItems_ReturnsCartWithItems() throws Exception {
            // First add an item to cart
            AddCartItemRequest request = new AddCartItemRequest();
            request.setProductId(testProduct.getId());
            request.setQuantity(2);

            mockMvc.perform(post(CART_BASE_URL + "/items")
                    .header("Authorization", bearerToken(authToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // Then get the cart
            mockMvc.perform(get(CART_BASE_URL)
                            .header("Authorization", bearerToken(authToken))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items", hasSize(1)))
                    .andExpect(jsonPath("$.items[0].productId").value(testProduct.getId().toString()))
                    .andExpect(jsonPath("$.items[0].quantity").value(2))
                    .andExpect(jsonPath("$.totalItems").value(2));
        }

        @Test
        @DisplayName("Should return 401 without authentication")
        void getCart_WithoutAuth_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(get(CART_BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("POST /api/cart/items")
    class AddItemToCartTests {

        @Test
        @DisplayName("Should add item to cart successfully")
        void addItem_WithValidData_ReturnsUpdatedCart() throws Exception {
            AddCartItemRequest request = new AddCartItemRequest();
            request.setProductId(testProduct.getId());
            request.setQuantity(3);

            mockMvc.perform(post(CART_BASE_URL + "/items")
                            .header("Authorization", bearerToken(authToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items", hasSize(1)))
                    .andExpect(jsonPath("$.items[0].productId").value(testProduct.getId().toString()))
                    .andExpect(jsonPath("$.items[0].quantity").value(3))
                    .andExpect(jsonPath("$.items[0].productName").value(testProduct.getName()));
        }

        @Test
        @DisplayName("Should increase quantity when adding same product")
        void addItem_SameProductTwice_IncreasesQuantity() throws Exception {
            AddCartItemRequest request = new AddCartItemRequest();
            request.setProductId(testProduct.getId());
            request.setQuantity(2);

            // Add first time
            mockMvc.perform(post(CART_BASE_URL + "/items")
                    .header("Authorization", bearerToken(authToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // Add second time
            mockMvc.perform(post(CART_BASE_URL + "/items")
                            .header("Authorization", bearerToken(authToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items", hasSize(1)))
                    .andExpect(jsonPath("$.items[0].quantity").value(4)); // 2 + 2
        }

        @Test
        @DisplayName("Should add multiple different products")
        void addItem_MultipleDifferentProducts_AddsAll() throws Exception {
            AddCartItemRequest request1 = new AddCartItemRequest();
            request1.setProductId(testProduct.getId());
            request1.setQuantity(1);

            AddCartItemRequest request2 = new AddCartItemRequest();
            request2.setProductId(anotherProduct.getId());
            request2.setQuantity(2);

            // Add first product
            mockMvc.perform(post(CART_BASE_URL + "/items")
                    .header("Authorization", bearerToken(authToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request1)));

            // Add second product
            mockMvc.perform(post(CART_BASE_URL + "/items")
                            .header("Authorization", bearerToken(authToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request2)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items", hasSize(2)))
                    .andExpect(jsonPath("$.totalItems").value(3)); // 1 + 2
        }

        @Test
        @DisplayName("Should return 404 for non-existent product")
        void addItem_NonExistentProduct_ReturnsNotFound() throws Exception {
            AddCartItemRequest request = new AddCartItemRequest();
            request.setProductId(UUID.randomUUID());
            request.setQuantity(1);

            mockMvc.perform(post(CART_BASE_URL + "/items")
                            .header("Authorization", bearerToken(authToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 401 without authentication")
        void addItem_WithoutAuth_ReturnsUnauthorized() throws Exception {
            AddCartItemRequest request = new AddCartItemRequest();
            request.setProductId(testProduct.getId());
            request.setQuantity(1);

            mockMvc.perform(post(CART_BASE_URL + "/items")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("PUT /api/cart/items/{id}")
    class UpdateCartItemTests {

        @Test
        @DisplayName("Should update cart item quantity")
        void updateItem_WithValidQuantity_ReturnsUpdatedCart() throws Exception {
            // First add an item
            AddCartItemRequest addRequest = new AddCartItemRequest();
            addRequest.setProductId(testProduct.getId());
            addRequest.setQuantity(2);

            String response = mockMvc.perform(post(CART_BASE_URL + "/items")
                            .header("Authorization", bearerToken(authToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(addRequest)))
                    .andReturn().getResponse().getContentAsString();

            // Extract cart item ID from response
            String cartItemId = objectMapper.readTree(response)
                    .get("items").get(0).get("id").asText();

            // Update the quantity
            UpdateCartItemRequest updateRequest = new UpdateCartItemRequest(5);

            mockMvc.perform(put(CART_BASE_URL + "/items/" + cartItemId)
                            .header("Authorization", bearerToken(authToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items[0].quantity").value(5));
        }

        @Test
        @DisplayName("Should return 404 for non-existent cart item")
        void updateItem_NonExistentItem_ReturnsNotFound() throws Exception {
            UpdateCartItemRequest updateRequest = new UpdateCartItemRequest(5);

            mockMvc.perform(put(CART_BASE_URL + "/items/" + UUID.randomUUID())
                            .header("Authorization", bearerToken(authToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 401 without authentication")
        void updateItem_WithoutAuth_ReturnsUnauthorized() throws Exception {
            UpdateCartItemRequest updateRequest = new UpdateCartItemRequest(5);

            mockMvc.perform(put(CART_BASE_URL + "/items/" + UUID.randomUUID())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("DELETE /api/cart/items/{id}")
    class DeleteCartItemTests {

        @Test
        @DisplayName("Should delete cart item successfully")
        void deleteItem_ExistingItem_ReturnsUpdatedCart() throws Exception {
            // First add an item
            AddCartItemRequest addRequest = new AddCartItemRequest();
            addRequest.setProductId(testProduct.getId());
            addRequest.setQuantity(2);

            String response = mockMvc.perform(post(CART_BASE_URL + "/items")
                            .header("Authorization", bearerToken(authToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(addRequest)))
                    .andReturn().getResponse().getContentAsString();

            // Extract cart item ID from response
            String cartItemId = objectMapper.readTree(response)
                    .get("items").get(0).get("id").asText();

            // Delete the item
            mockMvc.perform(delete(CART_BASE_URL + "/items/" + cartItemId)
                            .header("Authorization", bearerToken(authToken))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items", hasSize(0)))
                    .andExpect(jsonPath("$.totalItems").value(0));
        }

        @Test
        @DisplayName("Should return 404 for non-existent cart item")
        void deleteItem_NonExistentItem_ReturnsNotFound() throws Exception {
            mockMvc.perform(delete(CART_BASE_URL + "/items/" + UUID.randomUUID())
                            .header("Authorization", bearerToken(authToken))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 401 without authentication")
        void deleteItem_WithoutAuth_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(delete(CART_BASE_URL + "/items/" + UUID.randomUUID())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("DELETE /api/cart")
    class EmptyCartTests {

        @Test
        @DisplayName("Should empty cart successfully")
        void emptyCart_WithItems_ReturnsEmptyCart() throws Exception {
            // Add items first
            AddCartItemRequest request = new AddCartItemRequest();
            request.setProductId(testProduct.getId());
            request.setQuantity(3);

            mockMvc.perform(post(CART_BASE_URL + "/items")
                    .header("Authorization", bearerToken(authToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // Empty the cart
            mockMvc.perform(delete(CART_BASE_URL)
                            .header("Authorization", bearerToken(authToken))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items", hasSize(0)))
                    .andExpect(jsonPath("$.totalAmount").value(0))
                    .andExpect(jsonPath("$.totalItems").value(0));
        }

        @Test
        @DisplayName("Should return empty cart when already empty")
        void emptyCart_AlreadyEmpty_ReturnsEmptyCart() throws Exception {
            mockMvc.perform(delete(CART_BASE_URL)
                            .header("Authorization", bearerToken(authToken))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items", hasSize(0)));
        }

        @Test
        @DisplayName("Should return 401 without authentication")
        void emptyCart_WithoutAuth_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(delete(CART_BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Cart Isolation Tests")
    class CartIsolationTests {

        @Test
        @DisplayName("Should maintain separate carts for different users")
        void cart_DifferentUsers_HaveSeparateCarts() throws Exception {
            // Create second user
            String uniqueEmail2 = TestDataFactory.generateUniqueEmail();
            User secondUser = User.builder()
                    .email(uniqueEmail2)
                    .password(passwordEncoder.encode("Password123!"))
                    .firstName("Second")
                    .lastName("User")
                    .role(Role.CUSTOMER)
                    .authProvider(AuthProvider.LOCAL)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            secondUser = userRepository.save(secondUser);
            String secondToken = generateToken(secondUser);

            // Add item to first user's cart
            AddCartItemRequest request1 = new AddCartItemRequest();
            request1.setProductId(testProduct.getId());
            request1.setQuantity(3);

            mockMvc.perform(post(CART_BASE_URL + "/items")
                    .header("Authorization", bearerToken(authToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request1)));

            // Add different item to second user's cart
            AddCartItemRequest request2 = new AddCartItemRequest();
            request2.setProductId(anotherProduct.getId());
            request2.setQuantity(1);

            mockMvc.perform(post(CART_BASE_URL + "/items")
                    .header("Authorization", bearerToken(secondToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request2)));

            // Verify first user's cart
            mockMvc.perform(get(CART_BASE_URL)
                            .header("Authorization", bearerToken(authToken)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items", hasSize(1)))
                    .andExpect(jsonPath("$.items[0].productId").value(testProduct.getId().toString()))
                    .andExpect(jsonPath("$.totalItems").value(3));

            // Verify second user's cart
            mockMvc.perform(get(CART_BASE_URL)
                            .header("Authorization", bearerToken(secondToken)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items", hasSize(1)))
                    .andExpect(jsonPath("$.items[0].productId").value(anotherProduct.getId().toString()))
                    .andExpect(jsonPath("$.totalItems").value(1));
        }
    }
}
