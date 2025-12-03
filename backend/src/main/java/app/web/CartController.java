package app.web;

import app.cart.dto.AddCartItemRequest;
import app.cart.dto.CartResponse;
import app.cart.service.CartService;
import app.cartitem.dto.UpdateCartItemRequest;
import app.cartitem.service.CartItemService;
import app.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("api/cart")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "Shopping cart management endpoints")
public class CartController {

    private final CartService cartService;
    private final CartItemService cartItemService;

    @Operation(summary = "Get user's cart", description = "Retrieve the current user's shopping cart with all items")
    @ApiResponse(responseCode = "200", description = "Cart retrieved successfully",
            content = @Content(schema = @Schema(implementation = CartResponse.class)))
    @GetMapping
    public ResponseEntity<CartResponse> getCart(@AuthenticationPrincipal CustomUserDetails userDetails) {
        CartResponse cart = cartService.getCart(userDetails.getId());
        return ResponseEntity.ok(cart);
    }

    @Operation(summary = "Add item to cart", description = "Add a product to the shopping cart or increase quantity if already exists")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Item added successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request or insufficient stock"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItemToCart(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody AddCartItemRequest request) {
        CartResponse cart = cartService.addItemToCart(userDetails.getId(), request);
        return ResponseEntity.ok(cart);
    }

    @Operation(summary = "Update cart item quantity", description = "Update the quantity of an item in the cart")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Item updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid quantity or insufficient stock"),
            @ApiResponse(responseCode = "404", description = "Cart item not found")
    })
    @PutMapping("/items/{id}")
    public ResponseEntity<CartResponse> updateCartItem(
            @Parameter(description = "Cart item ID") @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody UpdateCartItemRequest request) {

        cartItemService.updateCartItemQuantity(id, userDetails.getId(), request);

        CartResponse cart = cartService.getCart(userDetails.getId());
        return ResponseEntity.ok(cart);
    }

    @Operation(summary = "Remove item from cart", description = "Remove an item from the shopping cart")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Item removed successfully"),
            @ApiResponse(responseCode = "404", description = "Cart item not found")
    })
    @DeleteMapping("/items/{id}")
    public ResponseEntity<CartResponse> deleteCartItem(
            @Parameter(description = "Cart item ID") @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        cartItemService.deleteCartItem(id, userDetails.getId());

        CartResponse cart = cartService.getCart(userDetails.getId());
        return ResponseEntity.ok(cart);
    }

    @Operation(summary = "Empty cart", description = "Remove all items from the shopping cart")
    @ApiResponse(responseCode = "200", description = "Cart emptied successfully")
    @DeleteMapping
    public ResponseEntity<CartResponse> emptyCart(@AuthenticationPrincipal CustomUserDetails userDetails) {
        CartResponse cart = cartService.emptyCart(userDetails.getId());
        return ResponseEntity.ok(cart);
    }
}
