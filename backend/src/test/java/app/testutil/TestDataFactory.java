package app.testutil;

import app.cart.model.Cart;
import app.cartitem.model.CartItem;
import app.order.model.Order;
import app.order.model.OrderItem;
import app.order.model.OrderStatus;
import app.product.model.Category;
import app.product.model.Product;
import app.review.model.Review;
import app.user.dto.LoginRequest;
import app.user.dto.RegisterRequest;
import app.user.model.AuthProvider;
import app.user.model.Role;
import app.user.model.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Test data factory for creating consistent test fixtures.
 * Provides builder-pattern methods for creating test entities.
 */
public final class TestDataFactory {

    private TestDataFactory() {
        // Utility class
    }

    // ==================== User Test Data ====================

    public static User createUser() {
        return User.builder()
                .id(UUID.randomUUID())
                .email("testuser@example.com")
                .password("encodedPassword123")
                .firstName("Test")
                .lastName("User")
                .role(Role.CUSTOMER)
                .authProvider(AuthProvider.LOCAL)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static User createUser(String email) {
        return User.builder()
                .id(UUID.randomUUID())
                .email(email)
                .password("encodedPassword123")
                .firstName("Test")
                .lastName("User")
                .role(Role.CUSTOMER)
                .authProvider(AuthProvider.LOCAL)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static User createAdminUser() {
        return User.builder()
                .id(UUID.randomUUID())
                .email("admin@example.com")
                .password("encodedPassword123")
                .firstName("Admin")
                .lastName("User")
                .role(Role.ADMIN)
                .authProvider(AuthProvider.LOCAL)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static RegisterRequest createRegisterRequest() {
        return RegisterRequest.builder()
                .email("newuser@example.com")
                .password("Password123!")
                .firstName("New")
                .lastName("User")
                .role(Role.CUSTOMER)
                .build();
    }

    public static RegisterRequest createRegisterRequest(String email) {
        return RegisterRequest.builder()
                .email(email)
                .password("Password123!")
                .firstName("New")
                .lastName("User")
                .role(Role.CUSTOMER)
                .build();
    }

    public static LoginRequest createLoginRequest(String email, String password) {
        return LoginRequest.builder()
                .email(email)
                .password(password)
                .build();
    }

    // ==================== Product Test Data ====================

    public static Product createProduct() {
        return Product.builder()
                .id(UUID.randomUUID())
                .name("Test Protein Powder")
                .description("High-quality whey protein for muscle building")
                .price(new BigDecimal("49.99"))
                .imageUrl("https://example.com/protein.jpg")
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

    public static Product createProduct(String name, Category category, BigDecimal price) {
        return Product.builder()
                .id(UUID.randomUUID())
                .name(name)
                .description("Product description for " + name)
                .price(price)
                .imageUrl("https://example.com/" + name.toLowerCase().replace(" ", "-") + ".jpg")
                .category(category)
                .stockQuantity(100)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .reviews(new ArrayList<>())
                .cartItems(new ArrayList<>())
                .orderItems(new ArrayList<>())
                .build();
    }

    public static Product createProductWithStock(String name, int stockQuantity) {
        return Product.builder()
                .id(UUID.randomUUID())
                .name(name)
                .description("Product with specific stock")
                .price(new BigDecimal("29.99"))
                .category(Category.VITAMINS)
                .stockQuantity(stockQuantity)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .reviews(new ArrayList<>())
                .cartItems(new ArrayList<>())
                .orderItems(new ArrayList<>())
                .build();
    }

    public static Product createInactiveProduct() {
        Product product = createProduct();
        product.setName("Inactive Product");
        product.setActive(false);
        return product;
    }

    // ==================== Cart Test Data ====================

    public static Cart createCart(User user) {
        return Cart.builder()
                .id(UUID.randomUUID())
                .user(user)
                .items(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static CartItem createCartItem(Cart cart, Product product, int quantity) {
        return CartItem.builder()
                .id(UUID.randomUUID())
                .cart(cart)
                .product(product)
                .quantity(quantity)
                .price(product.getPrice())
                .build();
    }

    // ==================== Order Test Data ====================

    public static Order createOrder(User user) {
        return Order.builder()
                .id(UUID.randomUUID())
                .user(user)
                .orderNumber(generateOrderNumber())
                .totalAmount(new BigDecimal("99.99"))
                .status(OrderStatus.PENDING)
                .shippingAddress("123 Test Street, Test City, TC 12345")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .items(new ArrayList<>())
                .build();
    }

    public static Order createOrder(User user, OrderStatus status) {
        Order order = createOrder(user);
        order.setStatus(status);
        return order;
    }

    public static OrderItem createOrderItem(Order order, Product product, int quantity) {
        return OrderItem.builder()
                .id(UUID.randomUUID())
                .order(order)
                .product(product)
                .quantity(quantity)
                .price(product.getPrice())
                .build();
    }

    // ==================== Review Test Data ====================

    public static Review createReview(User user, Product product) {
        return Review.builder()
                .id(UUID.randomUUID())
                .user(user)
                .product(product)
                .rating(5)
                .comment("Excellent product! Highly recommended.")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static Review createReview(User user, Product product, int rating) {
        return Review.builder()
                .id(UUID.randomUUID())
                .user(user)
                .product(product)
                .rating(rating)
                .comment("Review with rating " + rating)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // ==================== Utility Methods ====================

    public static String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public static String generateUniqueEmail() {
        return "user-" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
    }
}

