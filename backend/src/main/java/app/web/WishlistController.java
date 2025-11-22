package app.web;

import app.security.CustomUserDetails;
import app.wishlist.dto.AddToWishlistRequest;
import app.wishlist.dto.WishlistCountResponse;
import app.wishlist.dto.WishlistResponse;
import app.wishlist.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistController {
    
    private final WishlistService wishlistService;
    
    @PostMapping
    public ResponseEntity<Map<String, String>> addToWishlist(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody AddToWishlistRequest request) {
        
        wishlistService.addToWishlist(userDetails.getId(), request.getProductId());
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Product added to wishlist successfully");
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @DeleteMapping("/{productId}")
    public ResponseEntity<Map<String, String>> removeFromWishlist(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID productId) {
        
        wishlistService.removeFromWishlist(userDetails.getId(), productId);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Product removed from wishlist successfully");
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    public ResponseEntity<WishlistResponse> getUserWishlist(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        
        WishlistResponse wishlist = wishlistService.getUserWishlist(userDetails.getId(), page, size);
        
        return ResponseEntity.ok(wishlist);
    }
    
    @GetMapping("/check/{productId}")
    public ResponseEntity<Map<String, Boolean>> checkIfInWishlist(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID productId) {
        
        boolean isInWishlist = wishlistService.isInWishlist(userDetails.getId(), productId);
        
        Map<String, Boolean> response = new HashMap<>();
        response.put("isInWishlist", isInWishlist);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/count")
    public ResponseEntity<WishlistCountResponse> getWishlistCount(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        long count = wishlistService.getWishlistCount(userDetails.getId());
        
        return ResponseEntity.ok(new WishlistCountResponse(count));
    }
}
