package app.notification.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateNotificationPreferencesRequest {

    @NotNull(message = "Order updates preference is required")
    private Boolean orderUpdates;
    
    @NotNull(message = "Shipping notifications preference is required")
    private Boolean shippingNotifications;

    @NotNull(message = "Promotional emails preference is required")
    private Boolean promotionalEmails;
    
    @NotNull(message = "Newsletter preference is required")
    private Boolean newsletter;

    @NotNull(message = "Product recommendations preference is required")
    private Boolean productRecommendations;
    
    @NotNull(message = "Price drop alerts preference is required")
    private Boolean priceDropAlerts;
    
    @NotNull(message = "Back in stock alerts preference is required")
    private Boolean backInStockAlerts;

    @NotNull(message = "Account security alerts preference is required")
    private Boolean accountSecurityAlerts;
    
    @NotNull(message = "Password reset emails preference is required")
    private Boolean passwordResetEmails;

    @NotNull(message = "Review reminders preference is required")
    private Boolean reviewReminders;
}
