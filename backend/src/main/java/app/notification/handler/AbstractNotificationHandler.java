package app.notification.handler;

import app.notification.dto.NotificationPreferencesResponse;
import app.notification.model.NotificationType;
import app.notification.service.EmailService;
import app.notification.service.NotificationPreferencesService;
import app.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

/**
 * Abstract base class for notification handlers.
 * Provides common functionality and template method pattern for processing notifications.
 * 
 * <p>Subclasses only need to implement the specific logic for their event type,
 * while common concerns (logging, error handling, preference checking) are handled here.</p>
 * 
 * @param <E> The event type this handler processes
 */
@RequiredArgsConstructor
@Slf4j
public abstract class AbstractNotificationHandler<E> implements NotificationHandler<E> {

    protected final NotificationService notificationService;
    protected final NotificationPreferencesService preferencesService;
    protected final EmailService emailService;

    @Override
    public void handle(E event) {
        String eventName = getEventType().getSimpleName();
        log.info("Handling {} - {}", eventName, getEventDescription(event));

        try {
            processEvent(event);
        } catch (Exception e) {
            log.error("Failed to handle {}: {}", eventName, getEventDescription(event), e);
        }
    }

    /**
     * Template method for processing the event.
     * Subclasses implement this to define specific handling logic.
     */
    protected abstract void processEvent(E event);

    /**
     * Returns a description of the event for logging purposes.
     */
    protected abstract String getEventDescription(E event);

    /**
     * Helper method to get user preferences.
     */
    protected NotificationPreferencesResponse getPreferences(UUID userId) {
        return preferencesService.getPreferences(userId);
    }

    /**
     * Helper method to create and send an in-app notification via WebSocket.
     */
    protected void createInAppNotification(UUID userId, NotificationType type, 
                                           String title, String message, 
                                           String actionUrl, String referenceId) {
        notificationService.createAndSend(userId, type, title, message, actionUrl, referenceId);
    }

    /**
     * Helper method to send an email.
     */
    protected void sendEmail(String to, String subject, String htmlContent) {
        emailService.sendEmail(to, subject, htmlContent);
        log.info("Email sent to: {} - Subject: {}", to, subject);
    }

    /**
     * Helper method to check if a preference is enabled and log if disabled.
     */
    protected boolean isPreferenceEnabled(boolean preferenceValue, String preferenceName, String userEmail) {
        if (!preferenceValue) {
            log.info("User {} has disabled {}", userEmail, preferenceName);
            return false;
        }
        return true;
    }
}
