package app.notification.mapper;

import app.notification.dto.NotificationPreferencesResponse;
import app.notification.dto.UpdateNotificationPreferencesRequest;
import app.notification.model.NotificationPreferences;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.UUID;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class NotificationPreferencesMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "orderUpdates", constant = "true")
    @Mapping(target = "shippingNotifications", constant = "true")
    @Mapping(target = "promotionalEmails", constant = "true")
    @Mapping(target = "newsletter", constant = "true")
    @Mapping(target = "productRecommendations", constant = "true")
    @Mapping(target = "priceDropAlerts", constant = "true")
    @Mapping(target = "backInStockAlerts", constant = "true")
    @Mapping(target = "accountSecurityAlerts", constant = "true")
    @Mapping(target = "passwordResetEmails", constant = "true")
    @Mapping(target = "reviewReminders", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public abstract NotificationPreferences createDefaultPreferences(UUID userId);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public abstract void updatePreferencesFromRequest(@MappingTarget NotificationPreferences preferences,
                                                       UpdateNotificationPreferencesRequest request);

    public abstract NotificationPreferencesResponse toResponse(NotificationPreferences preferences);
}

