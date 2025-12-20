package app.product.service;

import app.exception.BadRequestException;
import app.exception.ResourceNotFoundException;
import app.order.model.OrderItem;
import app.product.dto.ProductDetails;
import app.product.dto.ProductPageResponse;
import app.product.mapper.ProductMapper;
import app.product.model.Category;
import app.product.model.Product;
import app.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Unit Tests")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    private Product testProduct;
    private UUID productId;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        testProduct = Product.builder()
                .id(productId)
                .name("Test Protein Powder")
                .description("High quality whey protein")
                .price(new BigDecimal("49.99"))
                .category(Category.PROTEIN)
                .stockQuantity(100)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .reviews(new ArrayList<>())
                .cartItems(new ArrayList<>())
                .orderItems(new ArrayList<>())
                .build();
    }

    @Nested
    @DisplayName("getAllProducts Tests")
    class GetAllProductsTests {

        @Test
        @DisplayName("Should return paginated products")
        void getAllProducts_WithDefaultParams_ReturnsPagedProducts() {
            Page<Product> productPage = new PageImpl<>(List.of(testProduct));
            ProductPageResponse expectedResponse = ProductPageResponse.builder().build();

            when(productRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(productPage);
            when(productMapper.toPageResponse(productPage)).thenReturn(expectedResponse);

            ProductPageResponse result = productService.getAllProducts(
                    null, null, null, null, null, 0, 10, "createdAt", "desc"
            );

            assertThat(result).isNotNull();
            verify(productRepository).findAll(any(Specification.class), any(Pageable.class));
            verify(productMapper).toPageResponse(productPage);
        }

        @Test
        @DisplayName("Should filter products by category")
        void getAllProducts_WithCategoryFilter_ReturnsFilteredProducts() {
            Page<Product> productPage = new PageImpl<>(List.of(testProduct));
            ProductPageResponse expectedResponse = ProductPageResponse.builder().build();

            when(productRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(productPage);
            when(productMapper.toPageResponse(productPage)).thenReturn(expectedResponse);

            ProductPageResponse result = productService.getAllProducts(
                    null, Category.PROTEIN, null, null, null, 0, 10, "createdAt", "asc"
            );

            assertThat(result).isNotNull();
            verify(productRepository).findAll(any(Specification.class), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("getProductById Tests")
    class GetProductByIdTests {

        @Test
        @DisplayName("Should return product when found")
        void getProductById_WithExistingId_ReturnsProduct() {
            when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));

            Product result = productService.getProductById(productId);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(productId);
            verify(productRepository).findById(productId);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when product not found")
        void getProductById_WithNonExistentId_ThrowsException() {
            UUID nonExistentId = UUID.randomUUID();
            when(productRepository.findById(nonExistentId)).thenReturn(Optional.empty());

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
        void getProductDetailsById_WithExistingId_ReturnsProductDetails() {
            ProductDetails expectedDetails = ProductDetails.builder().build();
            when(productRepository.findByIdWithReviews(productId)).thenReturn(Optional.of(testProduct));
            when(productMapper.toProductDetails(testProduct)).thenReturn(expectedDetails);

            ProductDetails result = productService.getProductDetailsById(productId);

            assertThat(result).isNotNull();
            verify(productRepository).findByIdWithReviews(productId);
            verify(productMapper).toProductDetails(testProduct);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when product not found")
        void getProductDetailsById_WithNonExistentId_ThrowsException() {
            UUID nonExistentId = UUID.randomUUID();
            when(productRepository.findByIdWithReviews(nonExistentId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.getProductDetailsById(nonExistentId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getAllCategories Tests")
    class GetAllCategoriesTests {

        @Test
        @DisplayName("Should return all categories")
        void getAllCategories_ReturnsAllCategoryValues() {
            List<Category> result = productService.getAllCategories();

            assertThat(result).isNotNull();
            assertThat(result).containsExactly(Category.values());
        }
    }

    @Nested
    @DisplayName("createProduct Tests")
    class CreateProductTests {

        @Test
        @DisplayName("Should create product successfully")
        void createProduct_WithValidProduct_ReturnsSavedProduct() {
            when(productRepository.save(testProduct)).thenReturn(testProduct);

            Product result = productService.createProduct(testProduct);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(productId);
            verify(productRepository).save(testProduct);
        }
    }

    @Nested
    @DisplayName("updateProduct Tests")
    class UpdateProductTests {

        @Test
        @DisplayName("Should update product successfully")
        void updateProduct_WithValidProduct_ReturnsUpdatedProduct() {
            testProduct.setName("Updated Product Name");
            when(productRepository.save(testProduct)).thenReturn(testProduct);

            Product result = productService.updateProduct(testProduct);

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Updated Product Name");
            verify(productRepository).save(testProduct);
        }
    }

    @Nested
    @DisplayName("deleteProduct Tests")
    class DeleteProductTests {

        @Test
        @DisplayName("Should delete product successfully when no orders exist")
        void deleteProduct_WithNoOrders_DeletesProduct() {
            testProduct.setOrderItems(new ArrayList<>());
            when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));

            productService.deleteProduct(productId);

            verify(productRepository).delete(testProduct);
        }

        @Test
        @DisplayName("Should throw BadRequestException when product has orders")
        void deleteProduct_WithExistingOrders_ThrowsException() {
            OrderItem orderItem = new OrderItem();
            testProduct.setOrderItems(List.of(orderItem));
            when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));

            assertThatThrownBy(() -> productService.deleteProduct(productId))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Cannot delete product with existing orders");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when product not found")
        void deleteProduct_WithNonExistentId_ThrowsException() {
            UUID nonExistentId = UUID.randomUUID();
            when(productRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.deleteProduct(nonExistentId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("reserveInventory Tests")
    class ReserveInventoryTests {

        @Test
        @DisplayName("Should reserve inventory successfully when sufficient stock")
        void reserveInventory_WithSufficientStock_ReducesStock() {
            testProduct.setStockQuantity(50);
            when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
            when(productRepository.save(testProduct)).thenReturn(testProduct);

            productService.reserveInventory(productId, 10);

            assertThat(testProduct.getStockQuantity()).isEqualTo(40);
            verify(productRepository).save(testProduct);
        }

        @Test
        @DisplayName("Should throw BadRequestException when insufficient stock")
        void reserveInventory_WithInsufficientStock_ThrowsException() {
            testProduct.setStockQuantity(5);
            when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));

            assertThatThrownBy(() -> productService.reserveInventory(productId, 10))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Insufficient stock");
        }

        @Test
        @DisplayName("Should reserve inventory down to zero")
        void reserveInventory_ToZeroStock_SetsStockToZero() {
            testProduct.setStockQuantity(10);
            when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
            when(productRepository.save(testProduct)).thenReturn(testProduct);

            productService.reserveInventory(productId, 10);

            assertThat(testProduct.getStockQuantity()).isEqualTo(0);
            verify(productRepository).save(testProduct);
        }
    }

    @Nested
    @DisplayName("releaseInventory Tests")
    class ReleaseInventoryTests {

        @Test
        @DisplayName("Should release inventory successfully")
        void releaseInventory_WithValidQuantity_IncreasesStock() {
            testProduct.setStockQuantity(40);
            when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
            when(productRepository.save(testProduct)).thenReturn(testProduct);

            productService.releaseInventory(productId, 10);

            assertThat(testProduct.getStockQuantity()).isEqualTo(50);
            verify(productRepository).save(testProduct);
        }

        @Test
        @DisplayName("Should release inventory to previously zero stock")
        void releaseInventory_FromZeroStock_SetsPositiveStock() {
            testProduct.setStockQuantity(0);
            when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
            when(productRepository.save(testProduct)).thenReturn(testProduct);

            productService.releaseInventory(productId, 25);

            assertThat(testProduct.getStockQuantity()).isEqualTo(25);
            verify(productRepository).save(testProduct);
        }
    }

    @Nested
    @DisplayName("findLowStockProducts Tests")
    class FindLowStockProductsTests {

        @Test
        @DisplayName("Should return products with stock below threshold")
        void findLowStockProducts_WithLowStockItems_ReturnsProducts() {
            Product lowStockProduct = Product.builder()
                    .id(UUID.randomUUID())
                    .name("Low Stock Item")
                    .stockQuantity(5)
                    .build();

            when(productRepository.findLowStockProducts(10)).thenReturn(List.of(lowStockProduct));

            List<Product> result = productService.findLowStockProducts(10);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStockQuantity()).isLessThan(10);
            verify(productRepository).findLowStockProducts(10);
        }

        @Test
        @DisplayName("Should return empty list when no low stock products")
        void findLowStockProducts_WithNoLowStock_ReturnsEmptyList() {
            when(productRepository.findLowStockProducts(10)).thenReturn(List.of());

            List<Product> result = productService.findLowStockProducts(10);

            assertThat(result).isEmpty();
        }
    }
}

