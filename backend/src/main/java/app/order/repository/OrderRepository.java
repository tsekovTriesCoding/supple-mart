package app.order.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import app.order.model.Order;
import app.order.model.OrderStatus;

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

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status IN ('DELIVERED', 'PROCESSING', 'SHIPPED', 'PAID')")
    BigDecimal calculateTotalRevenue();

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = 'PENDING'")
    Long countPendingOrders();

    @Query("SELECT COALESCE(SUM(oi.quantity), 0) FROM Order o " +
           "JOIN o.items oi " +
           "WHERE oi.product.id = :productId " +
           "AND o.status IN ('DELIVERED', 'PROCESSING', 'SHIPPED')")
    Integer getTotalSalesByProductId(@Param("productId") UUID productId);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.user.id = :userId")
    Long countTotalOrdersByUser(@Param("userId") UUID userId);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.user.id = :userId AND o.status = :status")
    Long countOrdersByUserAndStatus(@Param("userId") UUID userId, @Param("status") OrderStatus status);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.user.id = :userId AND o.status IN ('DELIVERED', 'PROCESSING', 'SHIPPED', 'PAID')")
    BigDecimal calculateTotalSpentByUser(@Param("userId") UUID userId);

    /**
     * Find orders with a specific status that were last updated before the cutoff date.
     * Used for auto-updating order statuses (e.g., SHIPPED -> DELIVERED after X days).
     */
    @Query("SELECT o FROM Order o WHERE o.status = :status AND o.updatedAt < :cutoffDate")
    List<Order> findByStatusAndUpdatedBefore(
            @Param("status") OrderStatus status,
            @Param("cutoffDate") LocalDateTime cutoffDate
    );

    /**
     * Find delivered orders that haven't been reviewed within a timeframe.
     * Used for sending review reminder notifications.
     */
    @Query("SELECT o FROM Order o WHERE o.status = 'DELIVERED' " +
           "AND o.updatedAt BETWEEN :startDate AND :endDate " +
           "AND NOT EXISTS (SELECT r FROM Review r WHERE r.user = o.user AND r.product IN " +
           "(SELECT oi.product FROM OrderItem oi WHERE oi.order = o))")
    List<Order> findDeliveredOrdersWithoutReviews(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}
