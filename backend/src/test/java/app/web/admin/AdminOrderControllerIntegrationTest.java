package app.web.admin;

import app.BaseIntegrationTest;
import app.admin.dto.UpdateOrderStatusRequest;
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
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AdminOrderController.
 * Tests admin order management endpoints with a real database using Testcontainers.
 */
@DisplayName("Admin Order Controller Integration Tests")
class AdminOrderControllerIntegrationTest extends BaseIntegrationTest {

    private static final String ADMIN_ORDERS_BASE_URL = "/api/admin/orders";

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User adminUser;
    private User regularUser;
    private User customer;
    private Product testProduct;
    private Order testOrder;
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

        // Create customer
        String customerEmail = TestDataFactory.generateUniqueEmail();
        customer = User.builder()
                .email(customerEmail)
                .password(passwordEncoder.encode("CustomerPass123!"))
                .firstName("Customer")
                .lastName("Test")
                .role(Role.CUSTOMER)
                .authProvider(AuthProvider.LOCAL)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        customer = userRepository.save(customer);

        // Create test product
        testProduct = Product.builder()
                .name("Order Test Product")
                .description("Product for order testing")
                .price(new BigDecimal("49.99"))
                .category(Category.PROTEIN)
                .stockQuantity(100)
                .isActive(true)
                .build();
        testProduct = productRepository.save(testProduct);

        // Create test order
        testOrder = Order.builder()
                .user(customer)
                .orderNumber("ORD-DASHBOARD-" + System.currentTimeMillis())
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("49.99"))
                .shippingAddress("123 Test St, Test City, TC 12345")
                .items(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        OrderItem orderItem = OrderItem.builder()
                .order(testOrder)
                .product(testProduct)
                .quantity(1)
                .price(new BigDecimal("49.99"))
                .build();
        testOrder.getItems().add(orderItem);
        testOrder = orderRepository.save(testOrder);
    }

    @Nested
    @DisplayName("GET /api/admin/orders")
    class GetAllOrdersTests {

        @Test
        @DisplayName("Should return all orders for admin")
        void getAllOrders_AsAdmin_ReturnsOrders() throws Exception {
            mockMvc.perform(get(ADMIN_ORDERS_BASE_URL)
                            .header("Authorization", bearerToken(adminToken))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.totalElements").isNumber())
                    .andExpect(jsonPath("$.totalPages").isNumber());
        }

        @Test
        @DisplayName("Should filter orders by status")
        void getAllOrders_WithStatusFilter_ReturnsFilteredOrders() throws Exception {
            mockMvc.perform(get(ADMIN_ORDERS_BASE_URL)
                            .header("Authorization", bearerToken(adminToken))
                            .param("status", "PENDING")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }

        @Test
        @DisplayName("Should paginate orders")
        void getAllOrders_WithPagination_ReturnsPaginatedOrders() throws Exception {
            mockMvc.perform(get(ADMIN_ORDERS_BASE_URL)
                            .header("Authorization", bearerToken(adminToken))
                            .param("page", "0")
                            .param("limit", "5")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content", hasSize(lessThanOrEqualTo(5))))
                    .andExpect(jsonPath("$.pageSize").value(5))
                    .andExpect(jsonPath("$.currentPage").value(0));
        }

        @Test
        @DisplayName("Should return 403 for regular user")
        void getAllOrders_AsRegularUser_ReturnsForbidden() throws Exception {
            mockMvc.perform(get(ADMIN_ORDERS_BASE_URL)
                            .header("Authorization", bearerToken(userToken))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 401 without authentication")
        void getAllOrders_WithoutAuth_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(get(ADMIN_ORDERS_BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("PATCH /api/admin/orders/{orderId}/status")
    class UpdateOrderStatusTests {

        @Test
        @DisplayName("Should update order status to PROCESSING")
        void updateOrderStatus_ToProcessing_ReturnsUpdatedOrder() throws Exception {
            UpdateOrderStatusRequest request = new UpdateOrderStatusRequest();
            request.setStatus("PROCESSING");

            mockMvc.perform(patch(ADMIN_ORDERS_BASE_URL + "/" + testOrder.getId() + "/status")
                            .header("Authorization", bearerToken(adminToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("processing"));
        }

        @Test
        @DisplayName("Should update order status to SHIPPED")
        void updateOrderStatus_ToShipped_ReturnsUpdatedOrder() throws Exception {
            // First update to PROCESSING
            testOrder.setStatus(OrderStatus.PROCESSING);
            orderRepository.save(testOrder);

            UpdateOrderStatusRequest request = new UpdateOrderStatusRequest();
            request.setStatus("SHIPPED");

            mockMvc.perform(patch(ADMIN_ORDERS_BASE_URL + "/" + testOrder.getId() + "/status")
                            .header("Authorization", bearerToken(adminToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("shipped"));
        }

        @Test
        @DisplayName("Should update order status to DELIVERED")
        void updateOrderStatus_ToDelivered_ReturnsUpdatedOrder() throws Exception {
            // First update to SHIPPED
            testOrder.setStatus(OrderStatus.SHIPPED);
            orderRepository.save(testOrder);

            UpdateOrderStatusRequest request = new UpdateOrderStatusRequest();
            request.setStatus("DELIVERED");

            mockMvc.perform(patch(ADMIN_ORDERS_BASE_URL + "/" + testOrder.getId() + "/status")
                            .header("Authorization", bearerToken(adminToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("delivered"));
        }

        @Test
        @DisplayName("Should update order status to CANCELLED")
        void updateOrderStatus_ToCancelled_ReturnsUpdatedOrder() throws Exception {
            UpdateOrderStatusRequest request = new UpdateOrderStatusRequest();
            request.setStatus("CANCELLED");

            mockMvc.perform(patch(ADMIN_ORDERS_BASE_URL + "/" + testOrder.getId() + "/status")
                            .header("Authorization", bearerToken(adminToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("cancelled"));
        }

        @Test
        @DisplayName("Should return 404 for non-existent order")
        void updateOrderStatus_NonExistentOrder_ReturnsNotFound() throws Exception {
            UpdateOrderStatusRequest request = new UpdateOrderStatusRequest();
            request.setStatus("PROCESSING");

            mockMvc.perform(patch(ADMIN_ORDERS_BASE_URL + "/" + UUID.randomUUID() + "/status")
                            .header("Authorization", bearerToken(adminToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 400 for invalid UUID format")
        void updateOrderStatus_InvalidUuidFormat_ReturnsBadRequest() throws Exception {
            UpdateOrderStatusRequest request = new UpdateOrderStatusRequest();
            request.setStatus("PROCESSING");

            mockMvc.perform(patch(ADMIN_ORDERS_BASE_URL + "/invalid-uuid/status")
                            .header("Authorization", bearerToken(adminToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 403 for regular user")
        void updateOrderStatus_AsRegularUser_ReturnsForbidden() throws Exception {
            UpdateOrderStatusRequest request = new UpdateOrderStatusRequest();
            request.setStatus("PROCESSING");

            mockMvc.perform(patch(ADMIN_ORDERS_BASE_URL + "/" + testOrder.getId() + "/status")
                            .header("Authorization", bearerToken(userToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 401 without authentication")
        void updateOrderStatus_WithoutAuth_ReturnsUnauthorized() throws Exception {
            UpdateOrderStatusRequest request = new UpdateOrderStatusRequest();
            request.setStatus("PROCESSING");

            mockMvc.perform(patch(ADMIN_ORDERS_BASE_URL + "/" + testOrder.getId() + "/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 400 for missing status")
        void updateOrderStatus_MissingStatus_ReturnsBadRequest() throws Exception {
            mockMvc.perform(patch(ADMIN_ORDERS_BASE_URL + "/" + testOrder.getId() + "/status")
                            .header("Authorization", bearerToken(adminToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }
    }
}
