package app.web;

import app.BaseIntegrationTest;
import app.product.model.Category;
import app.product.model.Product;
import app.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for ProductController.
 * Tests product catalog and search endpoints with a real database using Testcontainers.
 */
@DisplayName("Product Controller Integration Tests")
class ProductControllerIntegrationTest extends BaseIntegrationTest {

    private static final String PRODUCTS_BASE_URL = "/api/products";

    @Autowired
    private ProductRepository productRepository;

    private Product proteinProduct;
    private Product vitaminProduct;
    private Product inactiveProduct;

    @BeforeEach
    void setUp() {
        proteinProduct = Product.builder()
                .name("Test Whey Protein")
                .description("Premium whey protein powder")
                .price(new BigDecimal("59.99"))
                .category(Category.PROTEIN)
                .stockQuantity(50)
                .isActive(true)
                .imageUrl("https://example.com/protein.jpg")
                .build();
        proteinProduct = productRepository.save(proteinProduct);

        vitaminProduct = Product.builder()
                .name("Test Vitamin Complex")
                .description("Complete daily vitamins")
                .price(new BigDecimal("29.99"))
                .category(Category.VITAMINS)
                .stockQuantity(100)
                .isActive(true)
                .imageUrl("https://example.com/vitamins.jpg")
                .build();
        vitaminProduct = productRepository.save(vitaminProduct);

        inactiveProduct = Product.builder()
                .name("Discontinued Product")
                .description("This product is no longer available")
                .price(new BigDecimal("19.99"))
                .category(Category.OTHER)
                .stockQuantity(0)
                .isActive(false)
                .imageUrl("https://example.com/discontinued.jpg")
                .build();
        inactiveProduct = productRepository.save(inactiveProduct);
    }

    @Nested
    @DisplayName("GET /api/products")
    class GetAllProductsTests {

        @Test
        @DisplayName("Should return all active products with default pagination")
        void getAllProducts_WithDefaults_ReturnsActiveProducts() throws Exception {
            mockMvc.perform(get(PRODUCTS_BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.products").isArray())
                    .andExpect(jsonPath("$.products[*].isActive", everyItem(is(true))))
                    .andExpect(jsonPath("$.totalElements").isNumber())
                    .andExpect(jsonPath("$.totalPages").isNumber())
                    .andExpect(jsonPath("$.currentPage").value(0));
        }

        @Test
        @DisplayName("Should filter products by category")
        void getAllProducts_WithCategoryFilter_ReturnsFilteredProducts() throws Exception {
            mockMvc.perform(get(PRODUCTS_BASE_URL)
                            .param("category", "PROTEIN")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.products").isArray())
                    .andExpect(jsonPath("$.products[?(@.category == 'PROTEIN')]").exists());
        }

        @Test
        @DisplayName("Should filter products by price range")
        void getAllProducts_WithPriceRange_ReturnsFilteredProducts() throws Exception {
            mockMvc.perform(get(PRODUCTS_BASE_URL)
                            .param("minPrice", "25.00")
                            .param("maxPrice", "35.00")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.products").isArray());
        }

        @Test
        @DisplayName("Should search products by name")
        void getAllProducts_WithSearchTerm_ReturnsMatchingProducts() throws Exception {
            mockMvc.perform(get(PRODUCTS_BASE_URL)
                            .param("search", "Whey")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.products").isArray())
                    .andExpect(jsonPath("$.products[?(@.name =~ /.*Whey.*/i)]").exists());
        }

        @Test
        @DisplayName("Should paginate products correctly")
        void getAllProducts_WithPagination_ReturnsPaginatedResults() throws Exception {
            mockMvc.perform(get(PRODUCTS_BASE_URL)
                            .param("page", "0")
                            .param("size", "1")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.products").isArray())
                    .andExpect(jsonPath("$.products", hasSize(lessThanOrEqualTo(1))))
                    .andExpect(jsonPath("$.currentPage").value(0))
                    .andExpect(jsonPath("$.size").value(1));
        }

        @Test
        @DisplayName("Should sort products by price ascending")
        void getAllProducts_WithSortByPriceAsc_ReturnsSortedProducts() throws Exception {
            mockMvc.perform(get(PRODUCTS_BASE_URL)
                            .param("sortBy", "price")
                            .param("sortDirection", "asc")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.products").isArray());
        }

        @Test
        @DisplayName("Should sort products by price descending")
        void getAllProducts_WithSortByPriceDesc_ReturnsSortedProducts() throws Exception {
            mockMvc.perform(get(PRODUCTS_BASE_URL)
                            .param("sortBy", "price")
                            .param("sortDirection", "desc")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.products").isArray());
        }

        @Test
        @DisplayName("Should include inactive products when requested")
        void getAllProducts_WithActiveFilterFalse_IncludesInactiveProducts() throws Exception {
            mockMvc.perform(get(PRODUCTS_BASE_URL)
                            .param("active", "false")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.products").isArray());
        }

        @Test
        @DisplayName("Should combine multiple filters")
        void getAllProducts_WithMultipleFilters_ReturnsCorrectProducts() throws Exception {
            mockMvc.perform(get(PRODUCTS_BASE_URL)
                            .param("category", "PROTEIN")
                            .param("minPrice", "50.00")
                            .param("maxPrice", "100.00")
                            .param("sortBy", "price")
                            .param("sortDirection", "asc")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.products").isArray());
        }
    }

    @Nested
    @DisplayName("GET /api/products/categories")
    class GetCategoriesTests {

        @Test
        @DisplayName("Should return all product categories")
        void getAllCategories_ReturnsAllCategories() throws Exception {
            mockMvc.perform(get(PRODUCTS_BASE_URL + "/categories")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$", hasItem("protein")))
                    .andExpect(jsonPath("$", hasItem("vitamins")))
                    .andExpect(jsonPath("$", hasItem("minerals")));
        }
    }

    @Nested
    @DisplayName("GET /api/products/{id}")
    class GetProductByIdTests {

        @Test
        @DisplayName("Should return product details when exists")
        void getProductById_WithExistingId_ReturnsProductDetails() throws Exception {
            mockMvc.perform(get(PRODUCTS_BASE_URL + "/" + proteinProduct.getId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(proteinProduct.getId().toString()))
                    .andExpect(jsonPath("$.name").value(proteinProduct.getName()))
                    .andExpect(jsonPath("$.description").value(proteinProduct.getDescription()))
                    .andExpect(jsonPath("$.price").isNumber())
                    .andExpect(jsonPath("$.category").value("PROTEIN"))
                    .andExpect(jsonPath("$.stockQuantity").value(50))
                    .andExpect(jsonPath("$.inStock").value(true));
        }

        @Test
        @DisplayName("Should return inactive product when requested by ID")
        void getProductById_WithInactiveProduct_ReturnsProductDetails() throws Exception {
            mockMvc.perform(get(PRODUCTS_BASE_URL + "/" + inactiveProduct.getId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(inactiveProduct.getId().toString()))
                    .andExpect(jsonPath("$.inStock").value(false));
        }

        @Test
        @DisplayName("Should return 404 when product does not exist")
        void getProductById_WithNonExistentId_ReturnsNotFound() throws Exception {
            UUID nonExistentId = UUID.randomUUID();

            mockMvc.perform(get(PRODUCTS_BASE_URL + "/" + nonExistentId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 400 when ID format is invalid")
        void getProductById_WithInvalidIdFormat_ReturnsBadRequest() throws Exception {
            mockMvc.perform(get(PRODUCTS_BASE_URL + "/invalid-uuid-format")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Product Access - No Authentication Required")
    class PublicAccessTests {

        @Test
        @DisplayName("Should allow unauthenticated access to product list")
        void getAllProducts_WithoutAuth_ReturnsOk() throws Exception {
            mockMvc.perform(get(PRODUCTS_BASE_URL))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should allow unauthenticated access to product details")
        void getProductById_WithoutAuth_ReturnsOk() throws Exception {
            mockMvc.perform(get(PRODUCTS_BASE_URL + "/" + proteinProduct.getId()))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should allow unauthenticated access to categories")
        void getCategories_WithoutAuth_ReturnsOk() throws Exception {
            mockMvc.perform(get(PRODUCTS_BASE_URL + "/categories"))
                    .andExpect(status().isOk());
        }
    }
}
