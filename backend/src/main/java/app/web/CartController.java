package app.web;

import app.cart.dto.AddCartItemRequest;
import app.cart.dto.CartResponse;
import app.cart.service.CartService;
import app.cartitem.dto.UpdateCartItemRequest;
import app.cartitem.service.CartItemService;
import app.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final CartItemService cartItemService;

    @GetMapping
    public ResponseEntity<CartResponse> getCart(@AuthenticationPrincipal CustomUserDetails userDetails) {
        CartResponse cart = cartService.getCart(userDetails.getId());
        return ResponseEntity.ok(cart);
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItemToCart(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody AddCartItemRequest request) {
        CartResponse cart = cartService.addItemToCart(userDetails.getId(), request);
        return ResponseEntity.ok(cart);
    }

    @PutMapping("/items/{id}")
    public ResponseEntity<CartResponse> updateCartItem(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody UpdateCartItemRequest request) {

        cartItemService.updateCartItemQuantity(id, userDetails.getId(), request);

        CartResponse cart = cartService.getCart(userDetails.getId());
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<CartResponse> deleteCartItem(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        cartItemService.deleteCartItem(id, userDetails.getId());

        CartResponse cart = cartService.getCart(userDetails.getId());
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping
    public ResponseEntity<CartResponse> emptyCart(@AuthenticationPrincipal CustomUserDetails userDetails) {
        CartResponse cart = cartService.emptyCart(userDetails.getId());
        return ResponseEntity.ok(cart);
    }
}
