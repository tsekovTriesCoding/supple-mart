package app.cart.service;

import app.cart.dto.AddCartItemRequest;
import app.cart.dto.CartDTO;
import app.cart.mapper.CartMapper;
import app.cart.model.Cart;
import app.cartitem.model.CartItem;
import app.cart.repository.CartRepository;
import app.product.model.Product;
import app.product.service.ProductService;
import app.user.model.User;
import app.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final UserService userService;
    private final ProductService productService;
    private final CartMapper cartMapper;

    @Transactional(readOnly = true)
    public CartDTO getCart(UUID userId) {
        User user = userService.getUserById(userId);

        Cart cart = cartRepository.findByUserWithItems(user)
                .orElse(createEmptyCart(user));

        return cartMapper.toCartDTO(cart);
    }

    @Transactional
    public CartDTO addItemToCart(UUID userId, AddCartItemRequest request) {
        User user = userService.getUserById(userId);
        Product product = productService.getProductById(request.getProductId());

        if (!product.isActive()) {
            throw new RuntimeException("Product is not available");
        }

        if (product.getStockQuantity() < request.getQuantity()) {
            throw new RuntimeException("Insufficient stock");
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
                throw new RuntimeException("Insufficient stock");
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
        return cartMapper.toCartDTO(savedCart);
    }

    @Transactional
    public CartDTO emptyCart(UUID userId) {
        User user = userService.getUserById(userId);

        Cart cart = cartRepository.findByUserWithItems(user)
                .orElse(createEmptyCart(user));

        cart.getItems().clear();

        Cart savedCart = cartRepository.save(cart);
        return cartMapper.toCartDTO(savedCart);
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
