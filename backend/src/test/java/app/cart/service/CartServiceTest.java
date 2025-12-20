package app.cart.service;

import app.cart.dto.AddCartItemRequest;
import app.cart.dto.CartResponse;
import app.cart.mapper.CartMapper;
import app.cart.model.Cart;
import app.cart.repository.CartRepository;
import app.cartitem.model.CartItem;
import app.exception.BadRequestException;
import app.exception.ResourceNotFoundException;
import app.product.model.Category;
import app.product.model.Product;
import app.product.service.ProductService;
import app.user.model.AuthProvider;
import app.user.model.Role;
import app.user.model.User;
import app.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CartService Unit Tests")
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private UserService userService;

    @Mock
    private ProductService productService;

    @Mock
    private CartMapper cartMapper;

    @InjectMocks
    private CartService cartService;

    private User testUser;
    private Product testProduct;
    private Cart testCart;
    private UUID userId;
    private UUID productId;
    private UUID cartId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        productId = UUID.randomUUID();
        cartId = UUID.randomUUID();

        testUser = User.builder()
                .id(userId)
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .role(Role.CUSTOMER)
                .authProvider(AuthProvider.LOCAL)
                .build();

        testProduct = Product.builder()
                .id(productId)
                .name("Test Product")
                .description("Test Description")
                .price(new BigDecimal("29.99"))
                .category(Category.PROTEIN)
                .stockQuantity(50)
                .isActive(true)
                .build();

        testCart = Cart.builder()
                .id(cartId)
                .user(testUser)
                .items(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("getCart Tests")
    class GetCartTests {

        @Test
        @DisplayName("Should return existing cart for user")
        void getCart_WithExistingCart_ReturnsCartResponse() {
            CartResponse expectedResponse = CartResponse.builder().build();
            when(userService.getUserById(userId)).thenReturn(testUser);
            when(cartRepository.findByUserWithItems(testUser)).thenReturn(Optional.of(testCart));
            when(cartMapper.toCartResponse(testCart)).thenReturn(expectedResponse);

            CartResponse result = cartService.getCart(userId);

            assertThat(result).isNotNull();
            verify(cartRepository).findByUserWithItems(testUser);
            verify(cartMapper).toCartResponse(testCart);
        }

        @Test
        @DisplayName("Should return empty cart when no cart exists")
        void getCart_WithNoExistingCart_ReturnsEmptyCartResponse() {
            CartResponse expectedResponse = CartResponse.builder().build();
            when(userService.getUserById(userId)).thenReturn(testUser);
            when(cartRepository.findByUserWithItems(testUser)).thenReturn(Optional.empty());
            when(cartMapper.toCartResponse(any(Cart.class))).thenReturn(expectedResponse);

            CartResponse result = cartService.getCart(userId);

            assertThat(result).isNotNull();
            verify(cartMapper).toCartResponse(any(Cart.class));
        }
    }

    @Nested
    @DisplayName("addItemToCart Tests")
    class AddItemToCartTests {

        @Test
        @DisplayName("Should add new item to empty cart")
        void addItemToCart_ToEmptyCart_AddsNewItem() {
            AddCartItemRequest request = new AddCartItemRequest();
            request.setProductId(productId);
            request.setQuantity(2);

            CartResponse expectedResponse = CartResponse.builder().build();

            when(userService.getUserById(userId)).thenReturn(testUser);
            when(productService.getProductById(productId)).thenReturn(testProduct);
            when(cartRepository.findByUserWithItems(testUser)).thenReturn(Optional.empty());
            when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> {
                Cart cart = invocation.getArgument(0);
                cart.setId(cartId);
                return cart;
            });
            when(cartMapper.toCartResponse(any(Cart.class))).thenReturn(expectedResponse);

            CartResponse result = cartService.addItemToCart(userId, request);

            assertThat(result).isNotNull();
            verify(cartRepository, times(2)).save(any(Cart.class));
        }

        @Test
        @DisplayName("Should add new item to existing cart")
        void addItemToCart_ToExistingCart_AddsNewItem() {
            AddCartItemRequest request = new AddCartItemRequest();
            request.setProductId(productId);
            request.setQuantity(2);

            CartResponse expectedResponse = CartResponse.builder().build();

            when(userService.getUserById(userId)).thenReturn(testUser);
            when(productService.getProductById(productId)).thenReturn(testProduct);
            when(cartRepository.findByUserWithItems(testUser)).thenReturn(Optional.of(testCart));
            when(cartRepository.save(testCart)).thenReturn(testCart);
            when(cartMapper.toCartResponse(testCart)).thenReturn(expectedResponse);

            CartResponse result = cartService.addItemToCart(userId, request);

            assertThat(result).isNotNull();
            assertThat(testCart.getItems()).hasSize(1);
            verify(cartRepository).save(testCart);
        }

        @Test
        @DisplayName("Should update quantity when adding existing product")
        void addItemToCart_WithExistingProduct_UpdatesQuantity() {
            CartItem existingItem = CartItem.builder()
                    .id(UUID.randomUUID())
                    .cart(testCart)
                    .product(testProduct)
                    .quantity(3)
                    .price(testProduct.getPrice())
                    .build();
            testCart.getItems().add(existingItem);

            AddCartItemRequest request = new AddCartItemRequest();
            request.setProductId(productId);
            request.setQuantity(2);

            CartResponse expectedResponse = CartResponse.builder().build();

            when(userService.getUserById(userId)).thenReturn(testUser);
            when(productService.getProductById(productId)).thenReturn(testProduct);
            when(cartRepository.findByUserWithItems(testUser)).thenReturn(Optional.of(testCart));
            when(cartRepository.save(testCart)).thenReturn(testCart);
            when(cartMapper.toCartResponse(testCart)).thenReturn(expectedResponse);

            CartResponse result = cartService.addItemToCart(userId, request);

            assertThat(result).isNotNull();
            assertThat(existingItem.getQuantity()).isEqualTo(5);
            verify(cartRepository).save(testCart);
        }

        @Test
        @DisplayName("Should throw BadRequestException when product is inactive")
        void addItemToCart_WithInactiveProduct_ThrowsException() {
            testProduct.setActive(false);
            AddCartItemRequest request = new AddCartItemRequest();
            request.setProductId(productId);
            request.setQuantity(1);

            when(userService.getUserById(userId)).thenReturn(testUser);
            when(productService.getProductById(productId)).thenReturn(testProduct);

            assertThatThrownBy(() -> cartService.addItemToCart(userId, request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("not available");
        }

        @Test
        @DisplayName("Should throw BadRequestException when insufficient stock")
        void addItemToCart_WithInsufficientStock_ThrowsException() {
            testProduct.setStockQuantity(5);
            AddCartItemRequest request = new AddCartItemRequest();
            request.setProductId(productId);
            request.setQuantity(10);

            when(userService.getUserById(userId)).thenReturn(testUser);
            when(productService.getProductById(productId)).thenReturn(testProduct);

            assertThatThrownBy(() -> cartService.addItemToCart(userId, request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Insufficient stock");
        }

        @Test
        @DisplayName("Should throw BadRequestException when combined quantity exceeds stock")
        void addItemToCart_WithExceedingCombinedQuantity_ThrowsException() {
            testProduct.setStockQuantity(10);

            CartItem existingItem = CartItem.builder()
                    .id(UUID.randomUUID())
                    .cart(testCart)
                    .product(testProduct)
                    .quantity(8)
                    .price(testProduct.getPrice())
                    .build();
            testCart.getItems().add(existingItem);

            AddCartItemRequest request = new AddCartItemRequest();
            request.setProductId(productId);
            request.setQuantity(5);

            when(userService.getUserById(userId)).thenReturn(testUser);
            when(productService.getProductById(productId)).thenReturn(testProduct);
            when(cartRepository.findByUserWithItems(testUser)).thenReturn(Optional.of(testCart));

            assertThatThrownBy(() -> cartService.addItemToCart(userId, request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Insufficient stock");
        }
    }

    @Nested
    @DisplayName("emptyCart Tests")
    class EmptyCartTests {

        @Test
        @DisplayName("Should empty cart successfully")
        void emptyCart_WithItemsInCart_ClearsAllItems() {
            CartItem item = CartItem.builder()
                    .id(UUID.randomUUID())
                    .cart(testCart)
                    .product(testProduct)
                    .quantity(2)
                    .build();
            testCart.getItems().add(item);

            CartResponse expectedResponse = CartResponse.builder().build();

            when(userService.getUserById(userId)).thenReturn(testUser);
            when(cartRepository.findByUserWithItems(testUser)).thenReturn(Optional.of(testCart));
            when(cartRepository.save(testCart)).thenReturn(testCart);
            when(cartMapper.toCartResponse(testCart)).thenReturn(expectedResponse);

            CartResponse result = cartService.emptyCart(userId);

            assertThat(result).isNotNull();
            assertThat(testCart.getItems()).isEmpty();
            verify(cartRepository).save(testCart);
        }
    }

    @Nested
    @DisplayName("getCartWithItemsForOrder Tests")
    class GetCartWithItemsForOrderTests {

        @Test
        @DisplayName("Should return cart with items for order")
        void getCartWithItemsForOrder_WithExistingCart_ReturnsCart() {
            when(userService.getUserById(userId)).thenReturn(testUser);
            when(cartRepository.findByUserWithItems(testUser)).thenReturn(Optional.of(testCart));

            Cart result = cartService.getCartWithItemsForOrder(userId);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(cartId);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when cart not found")
        void getCartWithItemsForOrder_WithNoCart_ThrowsException() {
            when(userService.getUserById(userId)).thenReturn(testUser);
            when(cartRepository.findByUserWithItems(testUser)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> cartService.getCartWithItemsForOrder(userId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Cart not found");
        }
    }

    @Nested
    @DisplayName("clearCartAfterOrder Tests")
    class ClearCartAfterOrderTests {

        @Test
        @DisplayName("Should clear cart after order successfully")
        void clearCartAfterOrder_WithExistingCart_ClearsCart() {
            CartItem item = CartItem.builder()
                    .id(UUID.randomUUID())
                    .cart(testCart)
                    .product(testProduct)
                    .quantity(2)
                    .build();
            testCart.getItems().add(item);

            when(userService.getUserById(userId)).thenReturn(testUser);
            when(cartRepository.findByUserWithItems(testUser)).thenReturn(Optional.of(testCart));
            when(cartRepository.save(testCart)).thenReturn(testCart);

            cartService.clearCartAfterOrder(userId);

            assertThat(testCart.getItems()).isEmpty();
            verify(cartRepository).save(testCart);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when cart not found")
        void clearCartAfterOrder_WithNoCart_ThrowsException() {
            when(userService.getUserById(userId)).thenReturn(testUser);
            when(cartRepository.findByUserWithItems(testUser)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> cartService.clearCartAfterOrder(userId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("findAbandonedCarts Tests")
    class FindAbandonedCartsTests {

        @Test
        @DisplayName("Should return abandoned carts")
        void findAbandonedCarts_WithOldCarts_ReturnsAbandonedCarts() {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(7);
            when(cartRepository.findAbandonedCarts(cutoffDate)).thenReturn(List.of(testCart));

            List<Cart> result = cartService.findAbandonedCarts(cutoffDate);

            assertThat(result).hasSize(1);
            verify(cartRepository).findAbandonedCarts(cutoffDate);
        }
    }

    @Nested
    @DisplayName("countCarts Tests")
    class CountCartsTests {

        @Test
        @DisplayName("Should return cart count")
        void countCarts_ReturnsCount() {
            when(cartRepository.count()).thenReturn(50L);

            long result = cartService.countCarts();

            assertThat(result).isEqualTo(50L);
            verify(cartRepository).count();
        }
    }
}
