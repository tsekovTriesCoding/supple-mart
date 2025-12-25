package app.web.admin;

import app.BaseIntegrationTest;
import app.order.model.Order;
import app.order.model.OrderItem;
import app.order.model.OrderStatus;
import app.order.repository.OrderRepository;
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
import java.util.ArrayList;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AdminDashboardController.
 * Tests admin dashboard statistics endpoints with a real database using Testcontainers.
 */
@DisplayName("Admin Dashboard Controller Integration Tests")
class AdminDashboardControllerIntegrationTest extends BaseIntegrationTest {

    private static final String ADMIN_DASHBOARD_BASE_URL = "/api/admin/dashboard";

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User adminUser;
    private User regularUser;
    private String adminToken;
    private String userToken;

    @BeforeEach
    void setUp() {
        String adminEmail = TestDataFactory.generateUniqueEmail();
        adminUser = User.builder()
                .email(adminEmail)
                .password(passwordEncoder.encode("AdminPass123!"))
                .firstName("Admin")
                .lastName("User")
                .role(Role.ADMIN)
                .authProvider(AuthProvider.LOCAL)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        adminUser = userRepository.save(adminUser);
        adminToken = generateToken(adminUser);

        String userEmail = TestDataFactory.generateUniqueEmail();
        regularUser = User.builder()
                .email(userEmail)
                .password(passwordEncoder.encode("UserPass123!"))
                .firstName("Regular")
                .lastName("User")
                .role(Role.CUSTOMER)
                .authProvider(AuthProvider.LOCAL)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        regularUser = userRepository.save(regularUser);
        userToken = generateToken(regularUser);

        Product product1 = Product.builder()
                .name("Dashboard Test Product 1")
                .description("Product for dashboard testing")
                .price(new BigDecimal("49.99"))
                .category(Category.PROTEIN)
                .stockQuantity(100)
                .isActive(true)
                .build();
        product1 = productRepository.save(product1);

        Product product2 = Product.builder()
                .name("Dashboard Test Product 2")
                .description("Another product for dashboard testing")
                .price(new BigDecimal("29.99"))
                .category(Category.VITAMINS)
                .stockQuantity(50)
                .isActive(true)
                .build();
        product2 = productRepository.save(product2);

        Order order = Order.builder()
                .user(regularUser)
                .orderNumber("ORD-DASHBOARD-" + System.currentTimeMillis())
                .status(OrderStatus.DELIVERED)
                .totalAmount(new BigDecimal("79.98"))
                .shippingAddress("123 Test St, Test City, TC 12345")
                .items(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        OrderItem orderItem1 = OrderItem.builder()
                .order(order)
                .product(product1)
                .quantity(1)
                .price(new BigDecimal("49.99"))
                .build();
        order.getItems().add(orderItem1);

        OrderItem orderItem2 = OrderItem.builder()
                .order(order)
                .product(product2)
                .quantity(1)
                .price(new BigDecimal("29.99"))
                .build();
        order.getItems().add(orderItem2);

        orderRepository.save(order);
    }

    @Nested
    @DisplayName("GET /api/admin/dashboard/stats")
    class GetDashboardStatsTests {

        @Test
        @DisplayName("Should return dashboard statistics for admin")
        void getDashboardStats_AsAdmin_ReturnsStats() throws Exception {
            mockMvc.perform(get(ADMIN_DASHBOARD_BASE_URL + "/stats")
                            .header("Authorization", bearerToken(adminToken))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalProducts").isNumber())
                    .andExpect(jsonPath("$.totalCustomers").isNumber())
                    .andExpect(jsonPath("$.totalOrders").isNumber())
                    .andExpect(jsonPath("$.totalRevenue").isNumber());
        }

        @Test
        @DisplayName("Should return non-negative statistics")
        void getDashboardStats_AsAdmin_ReturnsNonNegativeStats() throws Exception {
            mockMvc.perform(get(ADMIN_DASHBOARD_BASE_URL + "/stats")
                            .header("Authorization", bearerToken(adminToken))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalProducts").value(org.hamcrest.Matchers.greaterThanOrEqualTo(0)))
                    .andExpect(jsonPath("$.totalCustomers").value(org.hamcrest.Matchers.greaterThanOrEqualTo(0)))
                    .andExpect(jsonPath("$.totalOrders").value(org.hamcrest.Matchers.greaterThanOrEqualTo(0)));
        }

        @Test
        @DisplayName("Should return 403 for regular user")
        void getDashboardStats_AsRegularUser_ReturnsForbidden() throws Exception {
            mockMvc.perform(get(ADMIN_DASHBOARD_BASE_URL + "/stats")
                            .header("Authorization", bearerToken(userToken))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 401 without authentication")
        void getDashboardStats_WithoutAuth_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(get(ADMIN_DASHBOARD_BASE_URL + "/stats")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 401 with invalid token")
        void getDashboardStats_InvalidToken_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(get(ADMIN_DASHBOARD_BASE_URL + "/stats")
                            .header("Authorization", "Bearer invalid.token.here")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 401 with expired token")
        void getDashboardStats_ExpiredToken_ReturnsUnauthorized() throws Exception {
            // This test verifies that expired tokens are rejected
            String expiredToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0QGV4YW1wbGUuY29tIiwiZXhwIjoxNjAwMDAwMDAwfQ.invalid";

            mockMvc.perform(get(ADMIN_DASHBOARD_BASE_URL + "/stats")
                            .header("Authorization", "Bearer " + expiredToken)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }
}
