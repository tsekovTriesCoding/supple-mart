package app.wishlist.mapper;

import app.product.model.Product;
import app.wishlist.dto.WishlistItemDTO;
import app.wishlist.dto.WishlistResponse;
import app.wishlist.model.Wishlist;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring")
public interface WishlistMapper {
    
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "productDescription", source = "product.description")
    @Mapping(target = "price", source = "product.price")
    @Mapping(target = "category", source = "product.category")
    @Mapping(target = "imageUrl", source = "product.imageUrl")
    @Mapping(target = "stockQuantity", source = "product.stockQuantity")
    @Mapping(target = "addedAt", source = "createdAt")
    @Mapping(target = "inStock", expression = "java(wishlist.getProduct().getStockQuantity() != null && wishlist.getProduct().getStockQuantity() > 0)")
    @Mapping(target = "averageRating", expression = "java(calculateAverageRating(wishlist.getProduct()))")
    @Mapping(target = "totalReviews", expression = "java(wishlist.getProduct().getReviews() != null ? wishlist.getProduct().getReviews().size() : 0)")
    WishlistItemDTO toWishlistItemDTO(Wishlist wishlist);
    
    default Double calculateAverageRating(Product product) {
        if (product.getReviews() == null || product.getReviews().isEmpty()) {
            return null;
        }
        return product.getReviews().stream()
                .mapToInt(app.review.model.Review::getRating)
                .average()
                .orElse(0.0);
    }

    default WishlistResponse toWishlistResponse(Page<Wishlist> wishlistPage) {
        if (wishlistPage == null) {
            return null;
        }
        
        List<WishlistItemDTO> items = wishlistPage.getContent().stream()
                .map(this::toWishlistItemDTO)
                .toList();
        
        return WishlistResponse.builder()
                .content(items)
                .currentPage(wishlistPage.getNumber())
                .pageSize(wishlistPage.getSize())
                .totalPages(wishlistPage.getTotalPages())
                .totalElements(wishlistPage.getTotalElements())
                .build();
    }
}
