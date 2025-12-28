package app.web.admin;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

import app.BaseIntegrationTest;
import app.admin.dto.CreateProductRequest;
import app.admin.dto.UpdateProductRequest;
import app.product.model.Category;
import app.product.model.Product;
import app.product.repository.ProductRepository;
import app.testutil.TestDataFactory;
import app.user.model.AuthProvider;
import app.user.model.Role;
import app.user.model.User;

/**
 * Integration tests for AdminProductController.
 * Tests admin product management endpoints with a real database using Testcontainers.
 */
@DisplayName("Admin Product Controller Integration Tests")
class AdminProductControllerIntegrationTest extends BaseIntegrationTest {

    private static final String ADMIN_PRODUCTS_BASE_URL = "/api/admin/products";

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User adminUser;
    private User regularUser;
    private Product testProduct;
    private String adminToken;
    private String userToken;

    @BeforeEach
    void setUp() {
        // Create admin user
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

        // Create regular user
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

        // Create test product
        testProduct = Product.builder()
                .name("Admin Test Product")
                .description("Product for admin testing")
                .price(new BigDecimal("49.99"))
                .category(Category.PROTEIN)
                .stockQuantity(100)
                .isActive(true)
                .build();
        testProduct = productRepository.save(testProduct);
    }

    @Nested
    @DisplayName("GET /api/admin/products")
    class GetAllProductsTests {

        @Test
        @DisplayName("Should return all products for admin")
        void getAllProducts_AsAdmin_ReturnsProducts() throws Exception {
            mockMvc.perform(get(ADMIN_PRODUCTS_BASE_URL)
                            .header("Authorization", bearerToken(adminToken))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.totalElements").isNumber())
                    .andExpect(jsonPath("$.totalPages").isNumber());
        }

        @Test
        @DisplayName("Should filter products by category")
        void getAllProducts_WithCategoryFilter_ReturnsFilteredProducts() throws Exception {
            mockMvc.perform(get(ADMIN_PRODUCTS_BASE_URL)
                            .header("Authorization", bearerToken(adminToken))
                            .param("category", "PROTEIN")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }

        @Test
        @DisplayName("Should filter products by search term")
        void getAllProducts_WithSearchFilter_ReturnsFilteredProducts() throws Exception {
            mockMvc.perform(get(ADMIN_PRODUCTS_BASE_URL)
                            .header("Authorization", bearerToken(adminToken))
                            .param("search", "Admin Test")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }

        @Test
        @DisplayName("Should filter products by price range")
        void getAllProducts_WithPriceFilter_ReturnsFilteredProducts() throws Exception {
            mockMvc.perform(get(ADMIN_PRODUCTS_BASE_URL)
                            .header("Authorization", bearerToken(adminToken))
                            .param("minPrice", "40.00")
                            .param("maxPrice", "60.00")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }

        @Test
        @DisplayName("Should filter products by active status")
        void getAllProducts_WithActiveFilter_ReturnsFilteredProducts() throws Exception {
            mockMvc.perform(get(ADMIN_PRODUCTS_BASE_URL)
                            .header("Authorization", bearerToken(adminToken))
                            .param("active", "true")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }

        @Test
        @DisplayName("Should paginate products")
        void getAllProducts_WithPagination_ReturnsPaginatedProducts() throws Exception {
            mockMvc.perform(get(ADMIN_PRODUCTS_BASE_URL)
                            .header("Authorization", bearerToken(adminToken))
                            .param("page", "0")
                            .param("size", "5")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content", hasSize(lessThanOrEqualTo(5))));
        }

        @Test
        @DisplayName("Should return 403 for regular user")
        void getAllProducts_AsRegularUser_ReturnsForbidden() throws Exception {
            mockMvc.perform(get(ADMIN_PRODUCTS_BASE_URL)
                            .header("Authorization", bearerToken(userToken))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 401 without authentication")
        void getAllProducts_WithoutAuth_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(get(ADMIN_PRODUCTS_BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("POST /api/admin/products")
    class CreateProductTests {

        @Test
        @DisplayName("Should create product successfully")
        void createProduct_ValidData_ReturnsCreated() throws Exception {
            CreateProductRequest request = CreateProductRequest.builder()
                    .name("New Admin Product")
                    .description("A brand new product created by admin")
                    .price(new BigDecimal("59.99"))
                    .category(Category.VITAMINS)
                    .stockQuantity(200)
                    .isActive(true)
                    .imageUrl("https://example.com/image.jpg")
                    .build();

            mockMvc.perform(post(ADMIN_PRODUCTS_BASE_URL)
                            .header("Authorization", bearerToken(adminToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name").value("New Admin Product"))
                    .andExpect(jsonPath("$.price").value(59.99))
                    .andExpect(jsonPath("$.category").value("VITAMINS"))
                    .andExpect(jsonPath("$.stockQuantity").value(200));
        }

        @Test
        @DisplayName("Should return 400 for missing name")
        void createProduct_MissingName_ReturnsBadRequest() throws Exception {
            CreateProductRequest request = CreateProductRequest.builder()
                    .description("A product without name")
                    .price(new BigDecimal("59.99"))
                    .category(Category.VITAMINS)
                    .stockQuantity(200)
                    .build();

            mockMvc.perform(post(ADMIN_PRODUCTS_BASE_URL)
                            .header("Authorization", bearerToken(adminToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 for negative price")
        void createProduct_NegativePrice_ReturnsBadRequest() throws Exception {
            CreateProductRequest request = CreateProductRequest.builder()
                    .name("Negative Price Product")
                    .description("This product has negative price")
                    .price(new BigDecimal("-10.00"))
                    .category(Category.VITAMINS)
                    .stockQuantity(200)
                    .build();

            mockMvc.perform(post(ADMIN_PRODUCTS_BASE_URL)
                            .header("Authorization", bearerToken(adminToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 for negative stock quantity")
        void createProduct_NegativeStock_ReturnsBadRequest() throws Exception {
            CreateProductRequest request = CreateProductRequest.builder()
                    .name("Negative Stock Product")
                    .description("This product has negative stock")
                    .price(new BigDecimal("59.99"))
                    .category(Category.VITAMINS)
                    .stockQuantity(-10)
                    .build();

            mockMvc.perform(post(ADMIN_PRODUCTS_BASE_URL)
                            .header("Authorization", bearerToken(adminToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 403 for regular user")
        void createProduct_AsRegularUser_ReturnsForbidden() throws Exception {
            CreateProductRequest request = CreateProductRequest.builder()
                    .name("Forbidden Product")
                    .description("Regular user cannot create products")
                    .price(new BigDecimal("59.99"))
                    .category(Category.VITAMINS)
                    .stockQuantity(200)
                    .isActive(true)
                    .build();

            mockMvc.perform(post(ADMIN_PRODUCTS_BASE_URL)
                            .header("Authorization", bearerToken(userToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 401 without authentication")
        void createProduct_WithoutAuth_ReturnsUnauthorized() throws Exception {
            CreateProductRequest request = CreateProductRequest.builder()
                    .name("Unauthorized Product")
                    .description("Cannot create without auth")
                    .price(new BigDecimal("59.99"))
                    .category(Category.VITAMINS)
                    .stockQuantity(200)
                    .build();

            mockMvc.perform(post(ADMIN_PRODUCTS_BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("PUT /api/admin/products/{id}")
    class UpdateProductTests {

        @Test
        @DisplayName("Should update product successfully")
        void updateProduct_ValidData_ReturnsUpdatedProduct() throws Exception {
            UpdateProductRequest request = UpdateProductRequest.builder()
                    .name("Updated Product Name")
                    .description("Updated product description")
                    .price(new BigDecimal("69.99"))
                    .category(Category.CREATINE)
                    .stockQuantity(150)
                    .isActive(true)
                    .build();

            mockMvc.perform(put(ADMIN_PRODUCTS_BASE_URL + "/" + testProduct.getId())
                            .header("Authorization", bearerToken(adminToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Updated Product Name"))
                    .andExpect(jsonPath("$.price").value(69.99))
                    .andExpect(jsonPath("$.category").value("CREATINE"));
        }

        @Test
        @DisplayName("Should deactivate product")
        void updateProduct_Deactivate_ReturnsUpdatedProduct() throws Exception {
            UpdateProductRequest request = UpdateProductRequest.builder()
                    .name(testProduct.getName())
                    .description(testProduct.getDescription())
                    .price(testProduct.getPrice())
                    .category(testProduct.getCategory())
                    .stockQuantity(testProduct.getStockQuantity())
                    .isActive(false)
                    .build();

            mockMvc.perform(put(ADMIN_PRODUCTS_BASE_URL + "/" + testProduct.getId())
                            .header("Authorization", bearerToken(adminToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isActive").value(false));
        }

        @Test
        @DisplayName("Should return 404 for non-existent product")
        void updateProduct_NonExistentProduct_ReturnsNotFound() throws Exception {
            UpdateProductRequest request = UpdateProductRequest.builder()
                    .name("Updated Name")
                    .description("Updated description")
                    .price(new BigDecimal("69.99"))
                    .category(Category.CREATINE)
                    .stockQuantity(150)
                    .isActive(true)
                    .build();

            mockMvc.perform(put(ADMIN_PRODUCTS_BASE_URL + "/" + UUID.randomUUID())
                            .header("Authorization", bearerToken(adminToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 400 for invalid UUID format")
        void updateProduct_InvalidUuidFormat_ReturnsBadRequest() throws Exception {
            UpdateProductRequest request = UpdateProductRequest.builder()
                    .name("Updated Name")
                    .description("Updated description")
                    .price(new BigDecimal("59.99"))
                    .category(Category.PROTEIN)
                    .stockQuantity(100)
                    .isActive(true)
                    .build();

            mockMvc.perform(put(ADMIN_PRODUCTS_BASE_URL + "/invalid-uuid")
                            .header("Authorization", bearerToken(adminToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 403 for regular user")
        void updateProduct_AsRegularUser_ReturnsForbidden() throws Exception {
            UpdateProductRequest request = UpdateProductRequest.builder()
                    .name("Forbidden Update")
                    .description("Regular user cannot update products")
                    .price(new BigDecimal("59.99"))
                    .category(Category.PROTEIN)
                    .stockQuantity(100)
                    .isActive(true)
                    .build();

            mockMvc.perform(put(ADMIN_PRODUCTS_BASE_URL + "/" + testProduct.getId())
                            .header("Authorization", bearerToken(userToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("DELETE /api/admin/products/{id}")
    class DeleteProductTests {

        @Test
        @DisplayName("Should delete product successfully")
        void deleteProduct_ExistingProduct_ReturnsNoContent() throws Exception {
            // Create a product that has no orders
            Product productToDelete = Product.builder()
                    .name("Product To Delete")
                    .description("This product will be deleted")
                    .price(new BigDecimal("29.99"))
                    .category(Category.OTHER)
                    .stockQuantity(10)
                    .isActive(true)
                    .build();
            productToDelete = productRepository.save(productToDelete);

            mockMvc.perform(delete(ADMIN_PRODUCTS_BASE_URL + "/" + productToDelete.getId())
                            .header("Authorization", bearerToken(adminToken))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Should return 404 for non-existent product")
        void deleteProduct_NonExistentProduct_ReturnsNotFound() throws Exception {
            mockMvc.perform(delete(ADMIN_PRODUCTS_BASE_URL + "/" + UUID.randomUUID())
                            .header("Authorization", bearerToken(adminToken))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 400 for invalid UUID format")
        void deleteProduct_InvalidUuidFormat_ReturnsBadRequest() throws Exception {
            mockMvc.perform(delete(ADMIN_PRODUCTS_BASE_URL + "/invalid-uuid")
                            .header("Authorization", bearerToken(adminToken))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 403 for regular user")
        void deleteProduct_AsRegularUser_ReturnsForbidden() throws Exception {
            mockMvc.perform(delete(ADMIN_PRODUCTS_BASE_URL + "/" + testProduct.getId())
                            .header("Authorization", bearerToken(userToken))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 401 without authentication")
        void deleteProduct_WithoutAuth_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(delete(ADMIN_PRODUCTS_BASE_URL + "/" + testProduct.getId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }
}
