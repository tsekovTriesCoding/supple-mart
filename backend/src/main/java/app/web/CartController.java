package app.web;

import app.cart.dto.AddCartItemRequest;
import app.cart.dto.CartDTO;
import app.cart.service.CartService;
import app.cart.item.dto.UpdateCartItemRequest;
import app.cart.item.service.CartItemService;
import app.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final CartItemService cartItemService;

    @GetMapping
    public ResponseEntity<CartDTO> getCart(@AuthenticationPrincipal CustomUserDetails userDetails) {
        CartDTO cart = cartService.getCart(userDetails.getId());
        return ResponseEntity.ok(cart);
    }

    @PostMapping("/items")
    public ResponseEntity<CartDTO> addItemToCart(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody AddCartItemRequest request) {
        CartDTO cart = cartService.addItemToCart(userDetails.getId(), request);
        return ResponseEntity.ok(cart);
    }

    @PutMapping("/items/{id}")
    public ResponseEntity<CartDTO> updateCartItem(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody UpdateCartItemRequest request) {

        cartItemService.updateCartItemQuantity(id, userDetails.getId(), request);

        CartDTO cart = cartService.getCart(userDetails.getId());
        return ResponseEntity.ok(cart);
    }
}
