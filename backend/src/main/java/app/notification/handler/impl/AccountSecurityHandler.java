package app.notification.handler.impl;

import app.notification.dto.NotificationPreferencesResponse;
import app.notification.event.AccountSecurityEvent;
import app.notification.handler.AbstractNotificationHandler;
import app.notification.model.NotificationType;
import app.notification.service.EmailService;
import app.notification.service.NotificationPreferencesService;
import app.notification.service.NotificationService;
import org.springframework.stereotype.Component;

/**
 * Handles AccountSecurityEvent - security alerts are always created as in-app notifications
 * (for audit purposes), but email is preference-based.
 */
@Component
public class AccountSecurityHandler extends AbstractNotificationHandler<AccountSecurityEvent> {

    public AccountSecurityHandler(NotificationService notificationService,
                                  NotificationPreferencesService preferencesService,
                                  EmailService emailService) {
        super(notificationService, preferencesService, emailService);
    }

    @Override
    public Class<AccountSecurityEvent> getEventType() {
        return AccountSecurityEvent.class;
    }

    @Override
    protected String getEventDescription(AccountSecurityEvent event) {
        return String.format("Security alert '%s' for user %s", event.getAlertType(), event.getUserEmail());
    }

    @Override
    protected void processEvent(AccountSecurityEvent event) {
        NotificationPreferencesResponse prefs = getPreferences(event.getUserId());

        // Always create in-app notification for security alerts (important for audit trail)
        createInAppNotification(
            event.getUserId(),
            NotificationType.ACCOUNT_SECURITY,
            "Security Alert ⚠️",
            event.getAlertType() + ": " + event.getDetails(),
            "/account",
            null
        );

        // Email is optional based on preferences
        if (!isPreferenceEnabled(prefs.getAccountSecurityAlerts(), "security alerts", event.getUserEmail())) {
            return;
        }

        String htmlContent = emailService.buildSecurityAlertEmail(
            event.getUserFirstName(),
            event.getAlertType(),
            event.getDetails()
        );

        sendEmail(
            event.getUserEmail(),
            "Security Alert: " + event.getAlertType(),
            htmlContent
        );
    }
}
