package app.web;

import app.security.CustomUserDetails;
import app.wishlist.dto.AddToWishlistRequest;
import app.wishlist.dto.WishlistCheckResponse;
import app.wishlist.dto.WishlistCountResponse;
import app.wishlist.dto.WishlistMessageResponse;
import app.wishlist.dto.WishlistResponse;
import app.wishlist.mapper.WishlistMapper;
import app.wishlist.service.WishlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
@Tag(name = "Wishlist", description = "Wishlist management endpoints")
public class WishlistController {
    
    private final WishlistService wishlistService;
    private final WishlistMapper wishlistMapper;

    @Operation(summary = "Add to wishlist", description = "Add a product to the user's wishlist")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Product added to wishlist"),
            @ApiResponse(responseCode = "400", description = "Product already in wishlist"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @PostMapping
    public ResponseEntity<WishlistMessageResponse> addToWishlist(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AddToWishlistRequest request) {
        
        wishlistService.addToWishlist(userDetails.getId(), request.getProductId());
        
        WishlistMessageResponse response = wishlistMapper.toMessageResponse("Product added to wishlist successfully");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @Operation(summary = "Remove from wishlist", description = "Remove a product from the user's wishlist")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product removed from wishlist"),
            @ApiResponse(responseCode = "404", description = "Product not found in wishlist")
    })
    @DeleteMapping("/{productId}")
    public ResponseEntity<WishlistMessageResponse> removeFromWishlist(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "Product ID") @PathVariable UUID productId) {
        
        wishlistService.removeFromWishlist(userDetails.getId(), productId);
        
        WishlistMessageResponse response = wishlistMapper.toMessageResponse("Product removed from wishlist successfully");

        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Get wishlist", description = "Retrieve paginated list of products in the user's wishlist")
    @ApiResponse(responseCode = "200", description = "Wishlist retrieved successfully")
    @GetMapping
    public ResponseEntity<WishlistResponse> getUserWishlist(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "Page number") @RequestParam(required = false) Integer page,
            @Parameter(description = "Page size") @RequestParam(required = false) Integer size) {
        
        WishlistResponse wishlist = wishlistService.getUserWishlist(userDetails.getId(), page, size);
        
        return ResponseEntity.ok(wishlist);
    }
    
    @Operation(summary = "Check if in wishlist", description = "Check if a specific product is in the user's wishlist")
    @ApiResponse(responseCode = "200", description = "Check completed successfully")
    @GetMapping("/check/{productId}")
    public ResponseEntity<WishlistCheckResponse> checkIfInWishlist(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "Product ID") @PathVariable UUID productId) {
        
        boolean isInWishlist = wishlistService.isInWishlist(userDetails.getId(), productId);
        
        WishlistCheckResponse response = wishlistMapper.toCheckResponse(isInWishlist);

        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Get wishlist count", description = "Get the number of items in the user's wishlist")
    @ApiResponse(responseCode = "200", description = "Count retrieved successfully")
    @GetMapping("/count")
    public ResponseEntity<WishlistCountResponse> getWishlistCount(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        long count = wishlistService.getWishlistCount(userDetails.getId());
        
        return ResponseEntity.ok(wishlistMapper.toCountResponse(count));
    }
}
