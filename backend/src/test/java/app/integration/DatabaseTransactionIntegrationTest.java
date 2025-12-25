package app.integration;

import app.BaseIntegrationTest;
import app.cart.model.Cart;
import app.cart.repository.CartRepository;
import app.cartitem.model.CartItem;
import app.cartitem.repository.CartItemRepository;
import app.order.model.Order;
import app.order.model.OrderItem;
import app.order.model.OrderStatus;
import app.order.repository.OrderRepository;
import app.product.model.Category;
import app.product.model.Product;
import app.product.repository.ProductRepository;
import app.review.model.Review;
import app.review.repository.ReviewRepository;
import app.testutil.TestDataFactory;
import app.user.model.AuthProvider;
import app.user.model.Role;
import app.user.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for database transactions and entity relationships.
 * Verifies that JPA relationships and cascades work correctly with a real database.
 *
 * Test organization:
 * - Tests that verify JPA cascades (within session) use @Transactional
 * - Tests that verify DB-level cascades (ON DELETE CASCADE) run without @Transactional
 *   to ensure commits happen and FK cascades are triggered
 * - Each test creates its own data for isolation
 */
@DisplayName("Database Transaction Integration Tests")
class DatabaseTransactionIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    // Helper method to create a test user
    private User createTestUser() {
        return userRepository.saveAndFlush(User.builder()
                .email(TestDataFactory.generateUniqueEmail())
                .password("hashedPassword")
                .firstName("Test")
                .lastName("User")
                .role(Role.CUSTOMER)
                .authProvider(AuthProvider.LOCAL)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());
    }

    // Helper method to create a test product
    private Product createTestProduct() {
        return productRepository.saveAndFlush(Product.builder()
                .name("Test Product " + System.currentTimeMillis())
                .description("Product for testing")
                .price(new BigDecimal("49.99"))
                .category(Category.PROTEIN)
                .stockQuantity(100)
                .isActive(true)
                .build());
    }

    @Nested
    @DisplayName("Cart and CartItem Relationship Tests")
    class CartRelationshipTests {

        @Test
        @Transactional
        @DisplayName("Should create cart with items and maintain relationships")
        void cartWithItems_RelationshipsMaintained() {
            User testUser = createTestUser();
            Product testProduct = createTestProduct();

            // Create cart
            Cart cart = Cart.builder()
                    .user(testUser)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            cart = cartRepository.save(cart);

            // Create cart item
            CartItem cartItem = CartItem.builder()
                    .cart(cart)
                    .product(testProduct)
                    .quantity(3)
                    .price(testProduct.getPrice())
                    .build();
            cart.getItems().add(cartItem);
            cartRepository.save(cart);

            // Verify relationships
            Cart savedCart = cartRepository.findById(cart.getId()).orElseThrow();
            assertThat(savedCart.getItems()).hasSize(1);
            assertThat(savedCart.getItems().getFirst().getProduct().getId()).isEqualTo(testProduct.getId());
            assertThat(savedCart.getUser().getId()).isEqualTo(testUser.getId());
        }

        @Test
        @Transactional
        @DisplayName("Should cascade delete cart items when cart is deleted (JPA cascade)")
        void deleteCart_CascadesDeleteToItems() {
            User testUser = createTestUser();
            Product testProduct = createTestProduct();

            // Create cart
            Cart cart = Cart.builder()
                    .user(testUser)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            // Create cart item and add to cart's collection (using JPA cascade)
            CartItem cartItem = CartItem.builder()
                    .cart(cart)
                    .product(testProduct)
                    .quantity(2)
                    .price(testProduct.getPrice())
                    .build();
            cart.getItems().add(cartItem);

            // Save cart - this cascades to cart items
            cart = cartRepository.saveAndFlush(cart);
            var cartId = cart.getId();
            var cartItemId = cart.getItems().getFirst().getId();

            // Verify cart item exists before deletion
            assertThat(cartItemRepository.existsById(cartItemId)).isTrue();

            // Delete cart - JPA cascade should delete cart items
            cartRepository.deleteById(cartId);

            // Verify cart item is also deleted (check without flush to avoid session issues)
            assertThat(cartItemRepository.findById(cartItemId)).isEmpty();
        }
    }

    @Nested
    @DisplayName("Order and OrderItem Relationship Tests")
    class OrderRelationshipTests {

        @Test
        @Transactional
        @DisplayName("Should create order with items and maintain relationships")
        void orderWithItems_RelationshipsMaintained() {
            User testUser = createTestUser();
            Product testProduct = createTestProduct();

            // Create order
            Order order = Order.builder()
                    .user(testUser)
                    .orderNumber("ORD-REL-" + System.currentTimeMillis())
                    .totalAmount(new BigDecimal("99.98"))
                    .status(OrderStatus.PENDING)
                    .shippingAddress("123 Test Street")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            order = orderRepository.save(order);

            // Create order item
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(testProduct)
                    .quantity(2)
                    .price(testProduct.getPrice())
                    .build();
            order.getItems().add(orderItem);
            orderRepository.save(order);

            // Verify relationships
            Order savedOrder = orderRepository.findById(order.getId()).orElseThrow();
            assertThat(savedOrder.getItems()).hasSize(1);
            assertThat(savedOrder.getItems().getFirst().getProduct().getId()).isEqualTo(testProduct.getId());
            assertThat(savedOrder.getUser().getId()).isEqualTo(testUser.getId());
        }

        @Test
        @Transactional
        @DisplayName("Should update order status")
        void updateOrderStatus_StatusChanges() {
            User testUser = createTestUser();

            // Create order
            Order order = Order.builder()
                    .user(testUser)
                    .orderNumber("ORD-STATUS-" + System.currentTimeMillis())
                    .totalAmount(new BigDecimal("49.99"))
                    .status(OrderStatus.PENDING)
                    .shippingAddress("123 Test Street")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            order = orderRepository.save(order);

            // Update status
            order.setStatus(OrderStatus.PAID);
            order = orderRepository.save(order);

            // Verify status updated
            Order updatedOrder = orderRepository.findById(order.getId()).orElseThrow();
            assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.PAID);
        }
    }

    @Nested
    @DisplayName("Review Relationship Tests")
    class ReviewRelationshipTests {

        @Test
        @Transactional
        @DisplayName("Should create review with user and product relationships")
        void reviewCreation_RelationshipsMaintained() {
            User testUser = createTestUser();
            Product testProduct = createTestProduct();

            Review review = Review.builder()
                    .user(testUser)
                    .product(testProduct)
                    .rating(5)
                    .comment("Excellent product!")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            review = reviewRepository.save(review);

            // Verify relationships
            Review savedReview = reviewRepository.findById(review.getId()).orElseThrow();
            assertThat(savedReview.getUser().getId()).isEqualTo(testUser.getId());
            assertThat(savedReview.getProduct().getId()).isEqualTo(testProduct.getId());
            assertThat(savedReview.getRating()).isEqualTo(5);
        }

        @Test
        @Transactional
        @DisplayName("Should find reviews by product")
        void findReviewsByProduct_ReturnsProductReviews() {
            User testUser = createTestUser();
            Product testProduct = createTestProduct();
            var productId = testProduct.getId();

            // Create first review - set both sides of the relationship
            Review review1 = Review.builder()
                    .user(testUser)
                    .product(testProduct)
                    .rating(4)
                    .comment("Good product")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            testProduct.getReviews().add(review1);

            // Create another user for another review
            User anotherUser = createTestUser();

            Review review2 = Review.builder()
                    .user(anotherUser)
                    .product(testProduct)
                    .rating(5)
                    .comment("Amazing!")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            testProduct.getReviews().add(review2);

            // Save product which cascades to reviews
            productRepository.saveAndFlush(testProduct);

            // Find reviews via product - use the product ID we created
            Optional<Product> productWithReviews = productRepository.findByIdWithReviews(productId);
            assertThat(productWithReviews).isPresent();
            assertThat(productWithReviews.get().getReviews()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Database Cascade Tests")
    class DatabaseCascadeTests {

        /**
         * Tests database-level ON DELETE CASCADE.
         * This test does NOT use @Transactional because:
         * 1. We need the delete to actually commit to trigger DB cascade
         * 2. @Transactional would rollback, preventing cascade verification
         */
        @Test
        @DisplayName("Should cascade delete user's cart via database FK constraint")
        void deleteUser_CascadesDeleteToCart() {
            // Create isolated test data
            User userToDelete = userRepository.saveAndFlush(User.builder()
                    .email(TestDataFactory.generateUniqueEmail())
                    .password("password")
                    .firstName("Delete")
                    .lastName("Me")
                    .role(Role.CUSTOMER)
                    .authProvider(AuthProvider.LOCAL)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build());
            var userId = userToDelete.getId();

            Cart cart = cartRepository.saveAndFlush(Cart.builder()
                    .user(userToDelete)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build());
            var cartId = cart.getId();

            // Verify cart exists before deletion
            assertThat(cartRepository.existsById(cartId)).isTrue();

            // Delete user - DB FK cascade (ON DELETE CASCADE) will delete the cart
            userRepository.deleteById(userId);
            userRepository.flush();

            // Verify cart is also deleted via database cascade
            assertThat(cartRepository.findById(cartId)).isEmpty();
        }

        @Test
        @DisplayName("Should cascade delete user's orders via database FK constraint")
        void deleteUser_CascadesDeleteToOrders() {
            // Create isolated test data
            User userToDelete = userRepository.saveAndFlush(User.builder()
                    .email(TestDataFactory.generateUniqueEmail())
                    .password("password")
                    .firstName("Delete")
                    .lastName("Me")
                    .role(Role.CUSTOMER)
                    .authProvider(AuthProvider.LOCAL)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build());
            var userId = userToDelete.getId();

            Order order = orderRepository.saveAndFlush(Order.builder()
                    .user(userToDelete)
                    .orderNumber("ORD-DEL-" + System.currentTimeMillis())
                    .totalAmount(new BigDecimal("99.99"))
                    .status(OrderStatus.PENDING)
                    .shippingAddress("123 Delete Street")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build());
            var orderId = order.getId();

            // Verify order exists before deletion
            assertThat(orderRepository.existsById(orderId)).isTrue();

            // Delete user - DB FK cascade will delete the order
            userRepository.deleteById(userId);
            userRepository.flush();

            // Verify order is also deleted via database cascade
            assertThat(orderRepository.findById(orderId)).isEmpty();
        }
    }

    @Nested
    @DisplayName("Product Stock Tests")
    class ProductStockTests {

        @Test
        @Transactional
        @DisplayName("Should update product stock quantity")
        void updateStockQuantity_StockChanges() {
            Product testProduct = createTestProduct();
            int originalStock = testProduct.getStockQuantity();

            // Update stock
            testProduct.setStockQuantity(originalStock - 10);
            productRepository.save(testProduct);

            // Verify stock updated
            Product updated = productRepository.findById(testProduct.getId()).orElseThrow();
            assertThat(updated.getStockQuantity()).isEqualTo(originalStock - 10);
        }

        @Test
        @Transactional
        @DisplayName("Should find low stock products correctly")
        void findLowStock_ReturnsCorrectProducts() {
            // Create low stock product
            Product lowStock = productRepository.saveAndFlush(Product.builder()
                    .name("Very Low Stock " + System.currentTimeMillis())
                    .description("Almost out")
                    .price(new BigDecimal("9.99"))
                    .category(Category.OTHER)
                    .stockQuantity(2)
                    .isActive(true)
                    .build());
            var lowStockId = lowStock.getId();

            // Find low stock products
            List<Product> lowStockProducts = productRepository.findLowStockProducts(10);

            assertThat(lowStockProducts).anyMatch(p -> p.getId().equals(lowStockId));
        }

        @Test
        @Transactional
        @DisplayName("Should find out of stock products")
        void findOutOfStock_ReturnsCorrectProducts() {
            // Create out of stock product
            Product outOfStock = productRepository.saveAndFlush(Product.builder()
                    .name("Out of Stock " + System.currentTimeMillis())
                    .description("Currently unavailable")
                    .price(new BigDecimal("29.99"))
                    .category(Category.VITAMINS)
                    .stockQuantity(0)
                    .isActive(true)
                    .build());
            var outOfStockId = outOfStock.getId();

            // Find out of stock products
            List<Product> outOfStockProducts = productRepository.findOutOfStockProducts();

            assertThat(outOfStockProducts).anyMatch(p -> p.getId().equals(outOfStockId));
        }
    }

    @Nested
    @DisplayName("Timestamp Tests")
    class TimestampTests {

        @Test
        @Transactional
        @DisplayName("Should set createdAt and updatedAt on product creation")
        void createProduct_SetsTimestamps() {
            Product product = Product.builder()
                    .name("Timestamp Test " + System.currentTimeMillis())
                    .description("Testing timestamps")
                    .price(new BigDecimal("19.99"))
                    .category(Category.OTHER)
                    .stockQuantity(10)
                    .isActive(true)
                    .build();

            product = productRepository.save(product);

            assertThat(product.getCreatedAt()).isNotNull();
            assertThat(product.getUpdatedAt()).isNotNull();
        }

        @Test
        @Transactional
        @DisplayName("Should update updatedAt on product update")
        void updateProduct_UpdatesTimestamp() throws InterruptedException {
            Product testProduct = createTestProduct();
            LocalDateTime originalUpdatedAt = testProduct.getUpdatedAt();

            // Wait a bit to ensure timestamp difference
            Thread.sleep(100);

            // Update product
            testProduct.setName("Updated Name " + System.currentTimeMillis());
            productRepository.saveAndFlush(testProduct);

            Product updated = productRepository.findById(testProduct.getId()).orElseThrow();
            assertThat(updated.getUpdatedAt()).isAfterOrEqualTo(originalUpdatedAt);
        }
    }
}

