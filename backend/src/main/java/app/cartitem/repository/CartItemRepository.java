package app.cartitem.repository;

import app.cartitem.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, UUID> {

    @Query("SELECT ci FROM CartItem ci JOIN FETCH ci.product WHERE ci.id = :cartItemId AND ci.cart.user.id = :userId")
    Optional<CartItem> findByIdAndUserId(@Param("cartItemId") UUID cartItemId, @Param("userId") UUID userId);
}
