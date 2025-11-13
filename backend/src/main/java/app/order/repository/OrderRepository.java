package app.order.repository;

import app.order.model.Order;
import app.order.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    Optional<Order> findByStripePaymentIntentId(String stripePaymentIntentId);

    @Query("SELECT o FROM Order o WHERE o.user.id = :userId " +
            "AND (:status IS NULL OR o.status = :status) " +
            "AND (:startDate IS NULL OR o.createdAt >= :startDate) " +
            "AND (:endDate IS NULL OR o.createdAt <= :endDate) " +
            "ORDER BY o.createdAt DESC")
    Page<Order> findUserOrdersWithFilters(
            @Param("userId") UUID userId,
            @Param("status") OrderStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    @Query("SELECT o FROM Order o WHERE " +
            "(:status IS NULL OR o.status = :status) " +
            "AND (:startDate IS NULL OR o.createdAt >= :startDate) " +
            "AND (:endDate IS NULL OR o.createdAt <= :endDate) " +
            "ORDER BY o.createdAt DESC")
    Page<Order> findAllOrdersWithFilters(
            @Param("status") OrderStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status IN ('DELIVERED', 'PROCESSING', 'SHIPPED')")
    BigDecimal calculateTotalRevenue();

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = 'PENDING'")
    Long countPendingOrders();

    @Query("SELECT COALESCE(SUM(oi.quantity), 0) FROM Order o " +
           "JOIN o.items oi " +
           "WHERE oi.product.id = :productId " +
           "AND o.status IN ('DELIVERED', 'PROCESSING', 'SHIPPED')")
    Integer getTotalSalesByProductId(@Param("productId") UUID productId);
}
