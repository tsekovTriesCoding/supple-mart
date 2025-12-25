package app.product.service;

import app.BaseIntegrationTest;
import app.exception.BadRequestException;
import app.exception.ResourceNotFoundException;
import app.order.model.Order;
import app.order.model.OrderItem;
import app.order.model.OrderStatus;
import app.order.repository.OrderRepository;
import app.product.dto.ProductDetails;
import app.product.dto.ProductPageResponse;
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
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for ProductService.
 * Tests service layer with a real database using Testcontainers.
 */
@DisplayName("Product Service Integration Tests")
class ProductServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    private Product proteinProduct;
    private Product vitaminProduct;
    private Product lowStockProduct;
    private Product inactiveProduct;

    @BeforeEach
    void setUp() {
        proteinProduct = Product.builder()
                .name("Integration Test Protein")
                .description("High quality protein for integration testing")
                .price(new BigDecimal("59.99"))
                .category(Category.PROTEIN)
                .stockQuantity(100)
                .isActive(true)
                .build();
        proteinProduct = productRepository.save(proteinProduct);

        vitaminProduct = Product.builder()
                .name("Integration Test Vitamins")
                .description("Essential vitamins for testing")
                .price(new BigDecimal("24.99"))
                .category(Category.VITAMINS)
                .stockQuantity(50)
                .isActive(true)
                .build();
        vitaminProduct = productRepository.save(vitaminProduct);

        lowStockProduct = Product.builder()
                .name("Low Stock Item")
                .description("Item with low stock")
                .price(new BigDecimal("19.99"))
                .category(Category.MINERALS)
                .stockQuantity(3)
                .isActive(true)
                .build();
        lowStockProduct = productRepository.save(lowStockProduct);

        inactiveProduct = Product.builder()
                .name("Inactive Test Product")
                .description("This product is inactive")
                .price(new BigDecimal("15.99"))
                .category(Category.OTHER)
                .stockQuantity(10)
                .isActive(false)
                .build();
        inactiveProduct = productRepository.save(inactiveProduct);
    }

    @Nested
    @DisplayName("getAllProducts Tests")
    class GetAllProductsTests {

        @Test
        @DisplayName("Should return paginated products with default filters")
        void getAllProducts_DefaultFilters_ReturnsPaginatedProducts() {
            ProductPageResponse result = productService.getAllProducts(
                    null, null, null, null, true, 0, 10, "name", "asc"
            );

            assertThat(result).isNotNull();
            assertThat(result.getProducts()).isNotEmpty();
            // Products should be returned (default filter is active=true)
            assertThat(result.getTotalElements()).isGreaterThan(0);
        }

        @Test
        @DisplayName("Should filter products by category")
        void getAllProducts_WithCategoryFilter_ReturnsFilteredProducts() {
            ProductPageResponse result = productService.getAllProducts(
                    null, Category.PROTEIN, null, null, true, 0, 10, "name", "asc"
            );

            assertThat(result).isNotNull();
            assertThat(result.getProducts()).allMatch(p -> p.getCategory() == Category.PROTEIN);
        }

        @Test
        @DisplayName("Should filter products by price range")
        void getAllProducts_WithPriceRange_ReturnsFilteredProducts() {
            ProductPageResponse result = productService.getAllProducts(
                    null, null, new BigDecimal("20.00"), new BigDecimal("30.00"),
                    true, 0, 10, "price", "asc"
            );

            assertThat(result).isNotNull();
            assertThat(result.getProducts()).allMatch(p -> {
                BigDecimal price = p.getPrice();
                return price.compareTo(new BigDecimal("20.00")) >= 0 &&
                       price.compareTo(new BigDecimal("30.00")) <= 0;
            });
        }

        @Test
        @DisplayName("Should search products by name")
        void getAllProducts_WithSearch_ReturnsMatchingProducts() {
            ProductPageResponse result = productService.getAllProducts(
                    "Protein", null, null, null, true, 0, 10, "name", "asc"
            );

            assertThat(result).isNotNull();
            assertThat(result.getProducts()).anyMatch(p ->
                    p.getName().toLowerCase().contains("protein")
            );
        }

        @Test
        @DisplayName("Should sort products by price descending")
        void getAllProducts_SortByPriceDesc_ReturnsSortedProducts() {
            ProductPageResponse result = productService.getAllProducts(
                    null, null, null, null, true, 0, 10, "price", "desc"
            );

            assertThat(result).isNotNull();
            var products = result.getProducts();
            for (int i = 0; i < products.size() - 1; i++) {
                assertThat(products.get(i).getPrice())
                        .isGreaterThanOrEqualTo(products.get(i + 1).getPrice());
            }
        }

        @Test
        @DisplayName("Should return different results with active filter")
        void getAllProducts_ActiveFilterToggle_ReturnsDifferentResults() {
            // Get active products count
            ProductPageResponse activeResult = productService.getAllProducts(
                    null, null, null, null, true, 0, 100, "name", "asc"
            );

            // Get all products (including inactive)
            ProductPageResponse allResult = productService.getAllProducts(
                    null, null, null, null, null, 0, 100, "name", "asc"
            );

            assertThat(allResult).isNotNull();
            // When active filter is null, should include all products
            assertThat(allResult.getTotalElements()).isGreaterThanOrEqualTo(activeResult.getTotalElements());
        }
    }

    @Nested
    @DisplayName("getProductById Tests")
    class GetProductByIdTests {

        @Test
        @DisplayName("Should return product when exists")
        void getProductById_ExistingProduct_ReturnsProduct() {
            Product result = productService.getProductById(proteinProduct.getId());

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(proteinProduct.getId());
            assertThat(result.getName()).isEqualTo("Integration Test Protein");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when product not found")
        void getProductById_NonExistent_ThrowsException() {
            UUID nonExistentId = UUID.randomUUID();

            assertThatThrownBy(() -> productService.getProductById(nonExistentId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining(nonExistentId.toString());
        }
    }

    @Nested
    @DisplayName("getProductDetailsById Tests")
    class GetProductDetailsByIdTests {

        @Test
        @DisplayName("Should return product details with reviews")
        void getProductDetailsById_ExistingProduct_ReturnsDetails() {
            ProductDetails result = productService.getProductDetailsById(proteinProduct.getId());

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(proteinProduct.getId());
            assertThat(result.getName()).isEqualTo("Integration Test Protein");
            assertThat(result.getReviews()).isNotNull();
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when product not found")
        void getProductDetailsById_NonExistent_ThrowsException() {
            UUID nonExistentId = UUID.randomUUID();

            assertThatThrownBy(() -> productService.getProductDetailsById(nonExistentId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getAllCategories Tests")
    class GetAllCategoriesTests {

        @Test
        @DisplayName("Should return all categories")
        void getAllCategories_ReturnsAllCategories() {
            List<Category> result = productService.getAllCategories();

            assertThat(result).isNotNull();
            assertThat(result).containsExactly(Category.values());
            assertThat(result).contains(Category.PROTEIN, Category.VITAMINS, Category.MINERALS);
        }
    }

    @Nested
    @DisplayName("createProduct Tests")
    class CreateProductTests {

        @Test
        @Transactional
        @DisplayName("Should create new product")
        void createProduct_ValidProduct_ReturnsSavedProduct() {
            Product newProduct = Product.builder()
                    .name("New Integration Test Product")
                    .description("Created during integration test")
                    .price(new BigDecimal("34.99"))
                    .category(Category.CREATINE)
                    .stockQuantity(25)
                    .isActive(true)
                    .build();

            Product result = productService.createProduct(newProduct);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isNotNull();
            assertThat(result.getName()).isEqualTo("New Integration Test Product");
            assertThat(result.getCreatedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("updateProduct Tests")
    class UpdateProductTests {

        @Test
        @Transactional
        @DisplayName("Should update existing product")
        void updateProduct_ExistingProduct_ReturnsUpdatedProduct() {
            vitaminProduct.setName("Updated Vitamin Name");
            vitaminProduct.setPrice(new BigDecimal("29.99"));

            Product result = productService.updateProduct(vitaminProduct);

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Updated Vitamin Name");
            assertThat(result.getPrice()).isEqualByComparingTo(new BigDecimal("29.99"));
        }
    }

    @Nested
    @DisplayName("deleteProduct Tests")
    class DeleteProductTests {

        @Test
        @Transactional
        @DisplayName("Should delete product without orders")
        void deleteProduct_NoOrders_DeletesProduct() {
            UUID productId = inactiveProduct.getId();

            productService.deleteProduct(productId);

            assertThatThrownBy(() -> productService.getProductById(productId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw BadRequestException when product has orders")
        void deleteProduct_WithOrders_ThrowsException() {
            // Create a user for the order
            User user = User.builder()
                    .email(TestDataFactory.generateUniqueEmail())
                    .password("password")
                    .firstName("Order")
                    .lastName("User")
                    .role(Role.CUSTOMER)
                    .authProvider(AuthProvider.LOCAL)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            user = userRepository.save(user);

            // Create an order with the product
            Order order = Order.builder()
                    .user(user)
                    .orderNumber("ORD-TEST-" + System.currentTimeMillis())
                    .totalAmount(proteinProduct.getPrice())
                    .status(OrderStatus.PENDING)
                    .shippingAddress("Test Address")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            order = orderRepository.save(order);

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(proteinProduct)
                    .quantity(1)
                    .price(proteinProduct.getPrice())
                    .build();
            order.getItems().add(orderItem);
            orderRepository.save(order);

            // Refresh the product to load the order items
            proteinProduct = productRepository.findById(proteinProduct.getId()).orElseThrow();

            assertThatThrownBy(() -> productService.deleteProduct(proteinProduct.getId()))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Cannot delete product with existing orders");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when deleting non-existent product")
        void deleteProduct_NonExistent_ThrowsException() {
            UUID nonExistentId = UUID.randomUUID();

            assertThatThrownBy(() -> productService.deleteProduct(nonExistentId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Inventory Management Tests")
    class InventoryManagementTests {

        @Test
        @DisplayName("Should find low stock products")
        void findLowStockProducts_ReturnsLowStockItems() {
            List<Product> result = productRepository.findLowStockProducts(10);

            assertThat(result).isNotEmpty();
            assertThat(result).allMatch(p -> p.getStockQuantity() < 10);
            assertThat(result).anyMatch(p -> p.getName().equals("Low Stock Item"));
        }

        @Test
        @DisplayName("Should count low stock products")
        void countLowStockProducts_ReturnsCorrectCount() {
            Long count = productRepository.countLowStockProducts();

            assertThat(count).isGreaterThanOrEqualTo(1L);
        }
    }
}

