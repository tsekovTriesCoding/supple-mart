package app.order.repository;

import app.order.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {

    @Query("SELECT COALESCE(SUM(oi.quantity), 0) FROM OrderItem oi " +
           "WHERE oi.product.id = :productId " +
           "AND oi.order.status IN ('DELIVERED', 'PROCESSING', 'SHIPPED')")
    Integer getTotalSalesByProductId(@Param("productId") UUID productId);
}

