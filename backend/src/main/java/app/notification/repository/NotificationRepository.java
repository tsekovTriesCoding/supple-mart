package app.notification.repository;

import app.notification.model.Notification;
import app.notification.model.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    Page<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(UUID userId);

    long countByUserIdAndIsReadFalse(UUID userId);

    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.type IN :types ORDER BY n.createdAt DESC")
    Page<Notification> findByUserIdAndTypeIn(
            @Param("userId") UUID userId,
            @Param("types") List<NotificationType> types,
            Pageable pageable
    );

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :readAt WHERE n.userId = :userId AND n.isRead = false")
    int markAllAsReadByUserId(@Param("userId") UUID userId, @Param("readAt") LocalDateTime readAt);

    @Modifying
    @Query("DELETE FROM Notification n WHERE n.createdAt < :cutoffDate")
    int deleteOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);

    @Query("SELECT n.type, COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.isRead = false GROUP BY n.type")
    List<Object[]> countUnreadByType(@Param("userId") UUID userId);
}
