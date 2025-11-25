package app.notification.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notification_preferences")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreferences {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "order_updates", nullable = false)
    private Boolean orderUpdates = true;

    @Column(name = "shipping_notifications", nullable = false)
    private Boolean shippingNotifications = true;

    @Column(name = "promotional_emails", nullable = false)
    private Boolean promotionalEmails = true;

    @Column(name = "newsletter", nullable = false)
    private Boolean newsletter = true;

    @Column(name = "product_recommendations", nullable = false)
    private Boolean productRecommendations = true;

    @Column(name = "price_drop_alerts", nullable = false)
    private Boolean priceDropAlerts = true;

    @Column(name = "back_in_stock_alerts", nullable = false)
    private Boolean backInStockAlerts = true;

    @Column(name = "account_security_alerts", nullable = false)
    private Boolean accountSecurityAlerts = true;

    @Column(name = "password_reset_emails", nullable = false)
    private Boolean passwordResetEmails = true;

    @Column(name = "review_reminders", nullable = false)
    private Boolean reviewReminders = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
