package app.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreferencesResponse {
    private UUID id;
    private UUID userId;

    private Boolean orderUpdates;
    private Boolean shippingNotifications;

    private Boolean promotionalEmails;
    private Boolean newsletter;

    private Boolean productRecommendations;
    private Boolean priceDropAlerts;
    private Boolean backInStockAlerts;

    private Boolean accountSecurityAlerts;
    private Boolean passwordResetEmails;

    private Boolean reviewReminders;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
