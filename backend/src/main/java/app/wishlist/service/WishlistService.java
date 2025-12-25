package app.wishlist.service;

import app.exception.DuplicateResourceException;
import app.exception.ResourceNotFoundException;
import app.product.model.Product;
import app.product.service.ProductService;
import app.user.model.User;
import app.user.service.UserService;
import app.wishlist.dto.WishlistResponse;
import app.wishlist.mapper.WishlistMapper;
import app.wishlist.model.Wishlist;
import app.wishlist.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WishlistService {
    
    private final WishlistRepository wishlistRepository;
    private final ProductService productService;
    private final UserService userService;
    private final WishlistMapper wishlistMapper;
    
    @Transactional
    public void addToWishlist(UUID userId, UUID productId) {
        User user = userService.getUserById(userId);

        Product product = productService.getProductById(productId);

        if (wishlistRepository.existsByUserIdAndProductId(userId, productId)) {
            throw new DuplicateResourceException("Product already in wishlist");
        }

        Wishlist wishlist = Wishlist.builder()
                .user(user)
                .product(product)
                .build();
        
        wishlistRepository.save(wishlist);
    }
    
    @Transactional
    public void removeFromWishlist(UUID userId, UUID productId) {
        Wishlist wishlist = wishlistRepository.findByUserIdAndProductId(userId, productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found in wishlist"));
        
        wishlistRepository.delete(wishlist);
    }
    
    @Transactional(readOnly = true)
    public WishlistResponse getUserWishlist(UUID userId, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(
                page != null ? page : 0,
                size != null ? size : 10,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        
        Page<Wishlist> wishlistPage = wishlistRepository.findByUserId(userId, pageable);
        return wishlistMapper.toWishlistResponse(wishlistPage);
    }
    
    @Transactional(readOnly = true)
    public boolean isInWishlist(UUID userId, UUID productId) {
        return wishlistRepository.existsByUserIdAndProductId(userId, productId);
    }
    
    @Transactional(readOnly = true)
    public long getWishlistCount(UUID userId) {
        return wishlistRepository.countByUserId(userId);
    }
}
