package app.web;

import app.security.CustomUserDetails;
import app.wishlist.dto.AddToWishlistRequest;
import app.wishlist.dto.WishlistCheckResponse;
import app.wishlist.dto.WishlistCountResponse;
import app.wishlist.dto.WishlistMessageResponse;
import app.wishlist.dto.WishlistResponse;
import app.wishlist.mapper.WishlistMapper;
import app.wishlist.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistController {
    
    private final WishlistService wishlistService;
    private final WishlistMapper wishlistMapper;

    @PostMapping
    public ResponseEntity<WishlistMessageResponse> addToWishlist(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody AddToWishlistRequest request) {
        
        wishlistService.addToWishlist(userDetails.getId(), request.getProductId());
        
        WishlistMessageResponse response = wishlistMapper.toMessageResponse("Product added to wishlist successfully");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @DeleteMapping("/{productId}")
    public ResponseEntity<WishlistMessageResponse> removeFromWishlist(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID productId) {
        
        wishlistService.removeFromWishlist(userDetails.getId(), productId);
        
        WishlistMessageResponse response = wishlistMapper.toMessageResponse("Product removed from wishlist successfully");

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
    public ResponseEntity<WishlistCheckResponse> checkIfInWishlist(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID productId) {
        
        boolean isInWishlist = wishlistService.isInWishlist(userDetails.getId(), productId);
        
        WishlistCheckResponse response = wishlistMapper.toCheckResponse(isInWishlist);

        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/count")
    public ResponseEntity<WishlistCountResponse> getWishlistCount(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        long count = wishlistService.getWishlistCount(userDetails.getId());
        
        return ResponseEntity.ok(wishlistMapper.toCountResponse(count));
    }
}
