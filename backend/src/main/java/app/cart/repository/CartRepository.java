package app.cart.repository;

import app.cart.model.Cart;
import app.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartRepository extends JpaRepository<Cart, UUID> {

    Optional<Cart> findByUser(User user);

    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items ci LEFT JOIN FETCH ci.product WHERE c.user = :user")
    Optional<Cart> findByUserWithItems(User user);

    /**
     * Find abandoned carts - carts with items that haven't been updated since the given time.
     */
    @Query("SELECT c FROM Cart c " +
           "JOIN FETCH c.user u " +
           "JOIN FETCH c.items ci " +
           "WHERE c.updatedAt < :abandonedSince " +
           "AND SIZE(c.items) > 0")
    List<Cart> findAbandonedCarts(@Param("abandonedSince") LocalDateTime abandonedSince);
}
