package app.web;

import app.cart.dto.AddCartItemRequest;
import app.cart.dto.CartDTO;
import app.cart.service.CartService;
import app.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

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
}
