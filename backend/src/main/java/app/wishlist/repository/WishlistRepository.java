package app.wishlist.repository;

import app.wishlist.model.Wishlist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, UUID> {
    
    @Query("SELECT w FROM Wishlist w JOIN FETCH w.product p WHERE w.user.id = :userId")
    Page<Wishlist> findByUserId(UUID userId, Pageable pageable);
    
    @Query("SELECT CASE WHEN COUNT(w) > 0 THEN true ELSE false END FROM Wishlist w WHERE w.user.id = :userId AND w.product.id = :productId")
    boolean existsByUserIdAndProductId(UUID userId, UUID productId);
    
    Optional<Wishlist> findByUserIdAndProductId(UUID userId, UUID productId);
    
    @Query("SELECT COUNT(w) FROM Wishlist w WHERE w.user.id = :userId")
    long countByUserId(UUID userId);
    
    @Query("SELECT w.user FROM Wishlist w WHERE w.product.id = :productId")
    java.util.List<app.user.model.User> findUsersByProductId(UUID productId);
}
