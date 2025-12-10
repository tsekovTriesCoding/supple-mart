package app.cart.service;

import app.cart.dto.AddCartItemRequest;
import app.cart.dto.CartResponse;
import app.cart.mapper.CartMapper;
import app.cart.model.Cart;
import app.cartitem.model.CartItem;
import app.cart.repository.CartRepository;
import app.exception.BadRequestException;
import app.exception.ResourceNotFoundException;
import app.product.model.Product;
import app.product.service.ProductService;
import app.user.model.User;
import app.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final UserService userService;
    private final ProductService productService;
    private final CartMapper cartMapper;

    @Transactional(readOnly = true)
    public CartResponse getCart(UUID userId) {
        User user = userService.getUserById(userId);

        Cart cart = cartRepository.findByUserWithItems(user)
                .orElse(createEmptyCart(user));

        return cartMapper.toCartResponse(cart);
    }

    @Transactional
    public CartResponse addItemToCart(UUID userId, AddCartItemRequest request) {
        User user = userService.getUserById(userId);
        Product product = productService.getProductById(request.getProductId());

        if (!product.isActive()) {
            throw new BadRequestException("Product is not available");
        }

        if (product.getStockQuantity() < request.getQuantity()) {
            throw new BadRequestException("Insufficient stock. Available: " + product.getStockQuantity());
        }

        Cart cart = cartRepository.findByUserWithItems(user)
                .orElseGet(() -> createAndSaveCart(user));

        CartItem existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            int newQuantity = existingItem.getQuantity() + request.getQuantity();
            if (product.getStockQuantity() < newQuantity) {
                throw new BadRequestException("Insufficient stock. Available: " + product.getStockQuantity());
            }
            existingItem.setQuantity(newQuantity);
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.getQuantity())
                    .price(product.getPrice())
                    .build();
            cart.getItems().add(newItem);
        }

        Cart savedCart = cartRepository.save(cart);
        return cartMapper.toCartResponse(savedCart);
    }

    @Transactional
    public CartResponse emptyCart(UUID userId) {
        User user = userService.getUserById(userId);

        Cart cart = cartRepository.findByUserWithItems(user)
                .orElse(createEmptyCart(user));

        cart.getItems().clear();

        Cart savedCart = cartRepository.save(cart);
        return cartMapper.toCartResponse(savedCart);
    }

    @Transactional(readOnly = true)
    public Cart getCartWithItemsForOrder(UUID userId) {
        User user = userService.getUserById(userId);
        return cartRepository.findByUserWithItems(user)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user with ID " + userId));
    }

    @Transactional
    public void clearCartAfterOrder(UUID userId) {
        User user = userService.getUserById(userId);
        Cart cart = cartRepository.findByUserWithItems(user)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user with ID " + userId));

        cart.getItems().clear();
        cartRepository.save(cart);
    }

    /**
     * Find abandoned carts - carts with items that haven't been updated since the cutoff date.
     * Used by scheduled tasks for sending reminder notifications.
     */
    @Transactional(readOnly = true)
    public List<Cart> findAbandonedCarts(LocalDateTime cutoffDate) {
        return cartRepository.findAbandonedCarts(cutoffDate);
    }

    /**
     * Count total number of carts.
     * Used for metrics/monitoring.
     */
    @Transactional(readOnly = true)
    public long countCarts() {
        return cartRepository.count();
    }

    private Cart createEmptyCart(User user) {
        return Cart.builder()
                .user(user)
                .items(new ArrayList<>())
                .build();
    }

    private Cart createAndSaveCart(User user) {
        Cart cart = Cart.builder()
                .user(user)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        return cartRepository.save(cart);
    }
}
