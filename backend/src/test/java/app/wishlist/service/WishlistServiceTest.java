package app.wishlist.service;

import app.exception.ResourceNotFoundException;
import app.product.model.Category;
import app.product.model.Product;
import app.product.service.ProductService;
import app.user.model.AuthProvider;
import app.user.model.Role;
import app.user.model.User;
import app.user.service.UserService;
import app.wishlist.dto.WishlistResponse;
import app.wishlist.mapper.WishlistMapper;
import app.wishlist.model.Wishlist;
import app.wishlist.repository.WishlistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WishlistService Unit Tests")
class WishlistServiceTest {

    @Mock
    private WishlistRepository wishlistRepository;

    @Mock
    private ProductService productService;

    @Mock
    private UserService userService;

    @Mock
    private WishlistMapper wishlistMapper;

    @InjectMocks
    private WishlistService wishlistService;

    private User testUser;
    private Product testProduct;
    private Wishlist testWishlist;
    private UUID userId;
    private UUID productId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        productId = UUID.randomUUID();

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
                .price(new BigDecimal("29.99"))
                .category(Category.PROTEIN)
                .isActive(true)
                .build();

        testWishlist = Wishlist.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .product(testProduct)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("addToWishlist Tests")
    class AddToWishlistTests {

        @Test
        @DisplayName("Should add product to wishlist successfully")
        void addToWishlist_WithValidProduct_AddsToWishlist() {
            when(userService.getUserById(userId)).thenReturn(testUser);
            when(productService.getProductById(productId)).thenReturn(testProduct);
            when(wishlistRepository.existsByUserIdAndProductId(userId, productId)).thenReturn(false);
            when(wishlistRepository.save(any(Wishlist.class))).thenReturn(testWishlist);

            wishlistService.addToWishlist(userId, productId);

            ArgumentCaptor<Wishlist> wishlistCaptor = ArgumentCaptor.forClass(Wishlist.class);
            verify(wishlistRepository).save(wishlistCaptor.capture());

            Wishlist savedWishlist = wishlistCaptor.getValue();
            assertThat(savedWishlist.getUser()).isEqualTo(testUser);
            assertThat(savedWishlist.getProduct()).isEqualTo(testProduct);
        }

        @Test
        @DisplayName("Should throw IllegalStateException when product already in wishlist")
        void addToWishlist_WithExistingProduct_ThrowsException() {
            when(userService.getUserById(userId)).thenReturn(testUser);
            when(productService.getProductById(productId)).thenReturn(testProduct);
            when(wishlistRepository.existsByUserIdAndProductId(userId, productId)).thenReturn(true);

            assertThatThrownBy(() -> wishlistService.addToWishlist(userId, productId))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("already in wishlist");
        }
    }

    @Nested
    @DisplayName("removeFromWishlist Tests")
    class RemoveFromWishlistTests {

        @Test
        @DisplayName("Should remove product from wishlist successfully")
        void removeFromWishlist_WithExistingProduct_RemovesFromWishlist() {
            when(wishlistRepository.findByUserIdAndProductId(userId, productId))
                    .thenReturn(Optional.of(testWishlist));

            wishlistService.removeFromWishlist(userId, productId);

            verify(wishlistRepository).delete(testWishlist);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when product not in wishlist")
        void removeFromWishlist_WithNonExistingProduct_ThrowsException() {
            when(wishlistRepository.findByUserIdAndProductId(userId, productId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> wishlistService.removeFromWishlist(userId, productId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("not found in wishlist");
        }
    }

    @Nested
    @DisplayName("getUserWishlist Tests")
    class GetUserWishlistTests {

        @Test
        @DisplayName("Should return paginated wishlist")
        void getUserWishlist_WithValidUser_ReturnsPaginatedWishlist() {
            Page<Wishlist> wishlistPage = new PageImpl<>(List.of(testWishlist));
            WishlistResponse expectedResponse = new WishlistResponse();

            when(wishlistRepository.findByUserId(eq(userId), any(Pageable.class))).thenReturn(wishlistPage);
            when(wishlistMapper.toWishlistResponse(wishlistPage)).thenReturn(expectedResponse);

            WishlistResponse result = wishlistService.getUserWishlist(userId, 0, 10);

            assertThat(result).isNotNull();
            verify(wishlistRepository).findByUserId(eq(userId), any(Pageable.class));
        }

        @Test
        @DisplayName("Should use default page values when null")
        void getUserWishlist_WithNullPageParams_UsesDefaults() {
            Page<Wishlist> wishlistPage = new PageImpl<>(List.of(testWishlist));
            WishlistResponse expectedResponse = new WishlistResponse();

            when(wishlistRepository.findByUserId(eq(userId), any(Pageable.class))).thenReturn(wishlistPage);
            when(wishlistMapper.toWishlistResponse(wishlistPage)).thenReturn(expectedResponse);

            WishlistResponse result = wishlistService.getUserWishlist(userId, null, null);

            assertThat(result).isNotNull();
            verify(wishlistRepository).findByUserId(eq(userId), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("isInWishlist Tests")
    class IsInWishlistTests {

        @Test
        @DisplayName("Should return true when product is in wishlist")
        void isInWishlist_WithExistingProduct_ReturnsTrue() {
            when(wishlistRepository.existsByUserIdAndProductId(userId, productId)).thenReturn(true);

            boolean result = wishlistService.isInWishlist(userId, productId);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false when product is not in wishlist")
        void isInWishlist_WithNonExistingProduct_ReturnsFalse() {
            when(wishlistRepository.existsByUserIdAndProductId(userId, productId)).thenReturn(false);

            boolean result = wishlistService.isInWishlist(userId, productId);

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("getWishlistCount Tests")
    class GetWishlistCountTests {

        @Test
        @DisplayName("Should return correct wishlist count")
        void getWishlistCount_WithItems_ReturnsCount() {
            when(wishlistRepository.countByUserId(userId)).thenReturn(5L);

            long result = wishlistService.getWishlistCount(userId);

            assertThat(result).isEqualTo(5L);
        }

        @Test
        @DisplayName("Should return zero when wishlist is empty")
        void getWishlistCount_WithEmptyWishlist_ReturnsZero() {
            when(wishlistRepository.countByUserId(userId)).thenReturn(0L);

            long result = wishlistService.getWishlistCount(userId);

            assertThat(result).isZero();
        }
    }
}
