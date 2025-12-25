package app.web;

import app.BaseIntegrationTest;
import app.cart.dto.AddCartItemRequest;
import app.order.dto.CreateOrderRequest;
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
 * Integration tests for OrderController.
 * Tests order management endpoints with a real database using Testcontainers.
 */
@DisplayName("Order Controller Integration Tests")
class OrderControllerIntegrationTest extends BaseIntegrationTest {

    private static final String ORDERS_BASE_URL = "/api/orders";

    @Autowired
    private ProductRepository productRepository;


    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private Product testProduct;
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

        // Create test product with sufficient stock
        testProduct = Product.builder()
                .name("Test Order Product")
                .description("Product for order testing")
                .price(new BigDecimal("49.99"))
                .category(Category.PROTEIN)
                .stockQuantity(100)
                .isActive(true)
                .build();
        testProduct = productRepository.save(testProduct);
    }

    @Nested
    @DisplayName("GET /api/orders")
    class GetUserOrdersTests {

        @Test
        @DisplayName("Should return empty orders for new user")
        void getUserOrders_ForNewUser_ReturnsEmptyList() throws Exception {
            mockMvc.perform(get(ORDERS_BASE_URL)
                            .header("Authorization", bearerToken(authToken))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.orders").isArray())
                    .andExpect(jsonPath("$.totalElements").value(0));
        }

        @Test
        @DisplayName("Should return user's orders")
        void getUserOrders_WithOrders_ReturnsOrdersList() throws Exception {
            // First create an order by adding to cart and placing order
            addItemToCart(testProduct.getId(), 2);
            createOrder("123 Test Street, Test City, TC 12345");

            mockMvc.perform(get(ORDERS_BASE_URL)
                            .header("Authorization", bearerToken(authToken))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.orders").isArray())
                    .andExpect(jsonPath("$.orders", hasSize(greaterThanOrEqualTo(1))))
                    .andExpect(jsonPath("$.orders[0].orderNumber").isNotEmpty())
                    .andExpect(jsonPath("$.orders[0].status").isNotEmpty());
        }

        @Test
        @DisplayName("Should filter orders by status")
        void getUserOrders_WithStatusFilter_ReturnsFilteredOrders() throws Exception {
            // Create an order
            addItemToCart(testProduct.getId(), 1);
            createOrder("123 Test Street");

            mockMvc.perform(get(ORDERS_BASE_URL)
                            .param("status", "PENDING")
                            .header("Authorization", bearerToken(authToken))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.orders").isArray());
        }

        @Test
        @DisplayName("Should paginate orders")
        void getUserOrders_WithPagination_ReturnsPaginatedResults() throws Exception {
            mockMvc.perform(get(ORDERS_BASE_URL)
                            .param("page", "0")
                            .param("limit", "5")
                            .header("Authorization", bearerToken(authToken))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.orders").isArray());
        }

        @Test
        @DisplayName("Should return 401 without authentication")
        void getUserOrders_WithoutAuth_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(get(ORDERS_BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/orders/stats")
    class GetOrderStatsTests {

        @Test
        @DisplayName("Should return order statistics")
        void getOrderStats_ReturnsStats() throws Exception {
            mockMvc.perform(get(ORDERS_BASE_URL + "/stats")
                            .header("Authorization", bearerToken(authToken))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalOrders").isNumber())
                    .andExpect(jsonPath("$.totalSpent").isNumber());
        }

        @Test
        @DisplayName("Should return 401 without authentication")
        void getOrderStats_WithoutAuth_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(get(ORDERS_BASE_URL + "/stats")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("POST /api/orders")
    class CreateOrderTests {

        @Test
        @DisplayName("Should create order from cart items")
        void createOrder_WithCartItems_ReturnsCreatedOrder() throws Exception {
            // Add items to cart first
            addItemToCart(testProduct.getId(), 2);

            CreateOrderRequest request = new CreateOrderRequest();
            request.setShippingAddress("456 Order Street, Order City, OC 67890");

            mockMvc.perform(post(ORDERS_BASE_URL)
                            .header("Authorization", bearerToken(authToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNotEmpty())
                    .andExpect(jsonPath("$.orderNumber").isNotEmpty())
                    .andExpect(jsonPath("$.status").value("pending"))
                    .andExpect(jsonPath("$.shippingAddress").value(request.getShippingAddress()))
                    .andExpect(jsonPath("$.items").isArray())
                    .andExpect(jsonPath("$.items", hasSize(1)));
        }

        @Test
        @DisplayName("Should fail to create order with empty cart")
        void createOrder_WithEmptyCart_ReturnsBadRequest() throws Exception {
            CreateOrderRequest request = new CreateOrderRequest();
            request.setShippingAddress("456 Order Street, Order City, OC 67890");

            mockMvc.perform(post(ORDERS_BASE_URL)
                            .header("Authorization", bearerToken(authToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should fail to create order without shipping address")
        void createOrder_WithoutShippingAddress_ReturnsBadRequest() throws Exception {
            addItemToCart(testProduct.getId(), 1);

            CreateOrderRequest request = new CreateOrderRequest();
            // Not setting shipping address

            mockMvc.perform(post(ORDERS_BASE_URL)
                            .header("Authorization", bearerToken(authToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 401 without authentication")
        void createOrder_WithoutAuth_ReturnsUnauthorized() throws Exception {
            CreateOrderRequest request = new CreateOrderRequest();
            request.setShippingAddress("456 Order Street");

            mockMvc.perform(post(ORDERS_BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should clear cart after successful order")
        void createOrder_Success_ClearsCart() throws Exception {
            addItemToCart(testProduct.getId(), 3);

            CreateOrderRequest request = new CreateOrderRequest();
            request.setShippingAddress("789 Clear Cart Street");

            // Create order
            mockMvc.perform(post(ORDERS_BASE_URL)
                            .header("Authorization", bearerToken(authToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());

            // Verify cart is empty
            mockMvc.perform(get("/api/cart")
                            .header("Authorization", bearerToken(authToken)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items", hasSize(0)));
        }
    }

    @Nested
    @DisplayName("PATCH /api/orders/{orderId}/cancel")
    class CancelOrderTests {

        @Test
        @DisplayName("Should cancel pending order")
        void cancelOrder_PendingOrder_ReturnsCancelledOrder() throws Exception {
            addItemToCart(testProduct.getId(), 1);
            String orderResponse = createOrder("123 Cancel Street");
            String orderId = objectMapper.readTree(orderResponse).get("id").asText();

            mockMvc.perform(patch(ORDERS_BASE_URL + "/" + orderId + "/cancel")
                            .header("Authorization", bearerToken(authToken))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("cancelled"));
        }

        @Test
        @DisplayName("Should return 404 for non-existent order")
        void cancelOrder_NonExistentOrder_ReturnsNotFound() throws Exception {
            mockMvc.perform(patch(ORDERS_BASE_URL + "/" + UUID.randomUUID() + "/cancel")
                            .header("Authorization", bearerToken(authToken))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 401 without authentication")
        void cancelOrder_WithoutAuth_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(patch(ORDERS_BASE_URL + "/" + UUID.randomUUID() + "/cancel")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Order Isolation Tests")
    class OrderIsolationTests {

        @Test
        @DisplayName("Users cannot access each other's orders")
        void getOrders_DifferentUser_CannotAccessOthersOrders() throws Exception {
            addItemToCart(testProduct.getId(), 1);
            createOrder("123 First User Street");

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

            // Second user should see no orders
            mockMvc.perform(get(ORDERS_BASE_URL)
                            .header("Authorization", bearerToken(secondToken))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.orders", hasSize(0)));
        }
    }

    // Helper methods
    private void addItemToCart(UUID productId, int quantity) throws Exception {
        AddCartItemRequest request = new AddCartItemRequest();
        request.setProductId(productId);
        request.setQuantity(quantity);

        mockMvc.perform(post("/api/cart/items")
                .header("Authorization", bearerToken(authToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }

    private String createOrder(String shippingAddress) throws Exception {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setShippingAddress(shippingAddress);

        return mockMvc.perform(post(ORDERS_BASE_URL)
                        .header("Authorization", bearerToken(authToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
    }
}
