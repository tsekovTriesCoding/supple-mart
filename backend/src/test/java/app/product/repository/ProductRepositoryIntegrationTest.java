package app.product.repository;

import app.BaseIntegrationTest;
import app.product.model.Category;
import app.product.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for ProductRepository.
 * Tests custom queries and JPA repository methods with a real database.
 */
@DisplayName("Product Repository Integration Tests")
class ProductRepositoryIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ProductRepository productRepository;

    private Product lowStockProduct;
    private Product outOfStockProduct;
    private Product normalStockProduct;
    private Product inactiveProduct;

    @BeforeEach
    void setUp() {
        // Create products with different stock levels
        lowStockProduct = Product.builder()
                .name("Low Stock Protein")
                .description("Running low on stock")
                .price(new BigDecimal("49.99"))
                .category(Category.PROTEIN)
                .stockQuantity(5)
                .isActive(true)
                .build();
        lowStockProduct = productRepository.save(lowStockProduct);

        outOfStockProduct = Product.builder()
                .name("Out of Stock Vitamins")
                .description("Currently out of stock")
                .price(new BigDecimal("29.99"))
                .category(Category.VITAMINS)
                .stockQuantity(0)
                .isActive(true)
                .build();
        outOfStockProduct = productRepository.save(outOfStockProduct);

        normalStockProduct = Product.builder()
                .name("Normal Stock Product")
                .description("Well stocked item")
                .price(new BigDecimal("39.99"))
                .category(Category.MINERALS)
                .stockQuantity(100)
                .isActive(true)
                .build();
        normalStockProduct = productRepository.save(normalStockProduct);

        inactiveProduct = Product.builder()
                .name("Inactive Product")
                .description("This product is inactive")
                .price(new BigDecimal("19.99"))
                .category(Category.OTHER)
                .stockQuantity(50)
                .isActive(false)
                .build();
        inactiveProduct = productRepository.save(inactiveProduct);
    }

    @Nested
    @DisplayName("findById Tests")
    class FindByIdTests {

        @Test
        @DisplayName("Should find product by ID when exists")
        void findById_ExistingProduct_ReturnsProduct() {
            Optional<Product> result = productRepository.findById(lowStockProduct.getId());

            assertThat(result).isPresent();
            assertThat(result.get().getName()).isEqualTo("Low Stock Protein");
            assertThat(result.get().getCategory()).isEqualTo(Category.PROTEIN);
        }

        @Test
        @DisplayName("Should return empty when product does not exist")
        void findById_NonExistentProduct_ReturnsEmpty() {
            Optional<Product> result = productRepository.findById(UUID.randomUUID());

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByIdWithReviews Tests")
    class FindByIdWithReviewsTests {

        @Test
        @DisplayName("Should find product with reviews loaded")
        void findByIdWithReviews_ExistingProduct_ReturnsProductWithReviews() {
            Optional<Product> result = productRepository.findByIdWithReviews(lowStockProduct.getId());

            assertThat(result).isPresent();
            assertThat(result.get().getName()).isEqualTo("Low Stock Protein");
            // Reviews should be initialized (not lazy loaded)
            assertThat(result.get().getReviews()).isNotNull();
        }

        @Test
        @DisplayName("Should return empty when product does not exist")
        void findByIdWithReviews_NonExistentProduct_ReturnsEmpty() {
            Optional<Product> result = productRepository.findByIdWithReviews(UUID.randomUUID());

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("countLowStockProducts Tests")
    class CountLowStockProductsTests {

        @Test
        @DisplayName("Should count products with stock below 10")
        void countLowStockProducts_ReturnsCorrectCount() {
            Long count = productRepository.countLowStockProducts();

            // lowStockProduct (5) and outOfStockProduct (0) have stock < 10
            assertThat(count).isGreaterThanOrEqualTo(2L);
        }
    }

    @Nested
    @DisplayName("findLowStockProducts Tests")
    class FindLowStockProductsTests {

        @Test
        @DisplayName("Should find active products with stock below threshold")
        void findLowStockProducts_ReturnsLowStockActiveProducts() {
            List<Product> result = productRepository.findLowStockProducts(10);

            assertThat(result).isNotEmpty();
            assertThat(result).allMatch(p -> p.getStockQuantity() < 10);
            assertThat(result).allMatch(Product::isActive);
            // Results should be ordered by stock quantity ascending
            for (int i = 0; i < result.size() - 1; i++) {
                assertThat(result.get(i).getStockQuantity())
                        .isLessThanOrEqualTo(result.get(i + 1).getStockQuantity());
            }
        }

        @Test
        @DisplayName("Should return empty list when no low stock products")
        void findLowStockProducts_WithHighThreshold_ReturnsAllApplicable() {
            List<Product> result = productRepository.findLowStockProducts(1);

            // Only outOfStockProduct has stock = 0, which is < 1
            assertThat(result).allMatch(p -> p.getStockQuantity() < 1);
        }
    }

    @Nested
    @DisplayName("findOutOfStockProducts Tests")
    class FindOutOfStockProductsTests {

        @Test
        @DisplayName("Should find active products with zero stock")
        void findOutOfStockProducts_ReturnsZeroStockActiveProducts() {
            List<Product> result = productRepository.findOutOfStockProducts();

            assertThat(result).isNotEmpty();
            assertThat(result).allMatch(p -> p.getStockQuantity() == 0);
            assertThat(result).allMatch(Product::isActive);
        }
    }

    @Nested
    @DisplayName("findAll with Pageable Tests")
    class FindAllPageableTests {

        @Test
        @DisplayName("Should return paginated results")
        void findAll_WithPageable_ReturnsPaginatedResults() {
            Pageable pageable = PageRequest.of(0, 2);

            Page<Product> result = productRepository.findAll(pageable);

            assertThat(result.getContent()).hasSizeLessThanOrEqualTo(2);
            assertThat(result.getNumber()).isEqualTo(0);
            assertThat(result.getSize()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should return correct page when requesting second page")
        void findAll_SecondPage_ReturnsSecondPageResults() {
            Pageable pageable = PageRequest.of(1, 2);

            Page<Product> result = productRepository.findAll(pageable);

            assertThat(result.getNumber()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("save Tests")
    class SaveTests {

        @Test
        @DisplayName("Should save new product with generated ID")
        void save_NewProduct_GeneratesIdAndSaves() {
            Product newProduct = Product.builder()
                    .name("New Creatine Product")
                    .description("Premium creatine supplement")
                    .price(new BigDecimal("34.99"))
                    .category(Category.CREATINE)
                    .stockQuantity(75)
                    .isActive(true)
                    .build();

            Product saved = productRepository.save(newProduct);

            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getName()).isEqualTo("New Creatine Product");
            assertThat(saved.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should update existing product")
        void save_ExistingProduct_UpdatesProduct() {
            lowStockProduct.setStockQuantity(25);
            lowStockProduct.setPrice(new BigDecimal("54.99"));

            Product updated = productRepository.save(lowStockProduct);

            assertThat(updated.getId()).isEqualTo(lowStockProduct.getId());
            assertThat(updated.getStockQuantity()).isEqualTo(25);
            assertThat(updated.getPrice()).isEqualByComparingTo(new BigDecimal("54.99"));
        }
    }

    @Nested
    @DisplayName("delete Tests")
    class DeleteTests {

        @Test
        @DisplayName("Should delete product by entity")
        void delete_ExistingProduct_RemovesFromDatabase() {
            UUID productId = inactiveProduct.getId();

            productRepository.delete(inactiveProduct);

            Optional<Product> result = productRepository.findById(productId);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should delete product by ID")
        void deleteById_ExistingProduct_RemovesFromDatabase() {
            UUID productId = inactiveProduct.getId();

            productRepository.deleteById(productId);

            Optional<Product> result = productRepository.findById(productId);
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("count Tests")
    class CountTests {

        @Test
        @DisplayName("Should return total count of products")
        void count_ReturnsCorrectCount() {
            long count = productRepository.count();

            assertThat(count).isGreaterThanOrEqualTo(4L); // At least our 4 test products
        }
    }

    @Nested
    @DisplayName("existsById Tests")
    class ExistsByIdTests {

        @Test
        @DisplayName("Should return true for existing product")
        void existsById_ExistingProduct_ReturnsTrue() {
            boolean exists = productRepository.existsById(lowStockProduct.getId());

            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("Should return false for non-existent product")
        void existsById_NonExistentProduct_ReturnsFalse() {
            boolean exists = productRepository.existsById(UUID.randomUUID());

            assertThat(exists).isFalse();
        }
    }
}

