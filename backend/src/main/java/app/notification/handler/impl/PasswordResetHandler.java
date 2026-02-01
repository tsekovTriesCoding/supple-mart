package app.notification.handler.impl;

import app.notification.dto.NotificationPreferencesResponse;
import app.notification.event.PasswordResetEvent;
import app.notification.handler.AbstractNotificationHandler;
import app.notification.service.EmailService;
import app.notification.service.NotificationPreferencesService;
import app.notification.service.NotificationService;
import org.springframework.stereotype.Component;

/**
 * Handles PasswordResetEvent - sends password reset email with token.
 */
@Component
public class PasswordResetHandler extends AbstractNotificationHandler<PasswordResetEvent> {

    public PasswordResetHandler(NotificationService notificationService,
                                NotificationPreferencesService preferencesService,
                                EmailService emailService) {
        super(notificationService, preferencesService, emailService);
    }

    @Override
    public Class<PasswordResetEvent> getEventType() {
        return PasswordResetEvent.class;
    }

    @Override
    protected String getEventDescription(PasswordResetEvent event) {
        return String.format("Password reset for user %s", event.getUserEmail());
    }

    @Override
    protected void processEvent(PasswordResetEvent event) {
        NotificationPreferencesResponse prefs = getPreferences(event.getUserId());

        if (!isPreferenceEnabled(prefs.getPasswordResetEmails(), "password reset emails", event.getUserEmail())) {
            return;
        }

        String htmlContent = emailService.buildPasswordResetEmail(
            event.getUserFirstName(),
            event.getResetToken()
        );

        sendEmail(
            event.getUserEmail(),
            "Password Reset Request",
            htmlContent
        );
    }
}
