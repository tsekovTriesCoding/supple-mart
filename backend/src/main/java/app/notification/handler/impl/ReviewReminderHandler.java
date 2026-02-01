package app.notification.handler.impl;

import app.notification.dto.NotificationPreferencesResponse;
import app.notification.event.ReviewReminderEvent;
import app.notification.handler.AbstractNotificationHandler;
import app.notification.service.EmailService;
import app.notification.service.NotificationPreferencesService;
import app.notification.service.NotificationService;
import org.springframework.stereotype.Component;

/**
 * Handles ReviewReminderEvent - reminds customers to review their purchases.
 */
@Component
public class ReviewReminderHandler extends AbstractNotificationHandler<ReviewReminderEvent> {

    public ReviewReminderHandler(NotificationService notificationService,
                                 NotificationPreferencesService preferencesService,
                                 EmailService emailService) {
        super(notificationService, preferencesService, emailService);
    }

    @Override
    public Class<ReviewReminderEvent> getEventType() {
        return ReviewReminderEvent.class;
    }

    @Override
    protected String getEventDescription(ReviewReminderEvent event) {
        return String.format("Review reminder for order #%s, user %s", event.getOrderNumber(), event.getUserEmail());
    }

    @Override
    protected void processEvent(ReviewReminderEvent event) {
        NotificationPreferencesResponse prefs = getPreferences(event.getUserId());

        if (!isPreferenceEnabled(prefs.getReviewReminders(), "review reminders", event.getUserEmail())) {
            return;
        }

        String htmlContent = emailService.buildReviewReminderEmail(
            event.getUserFirstName(),
            event.getOrderNumber()
        );

        sendEmail(
            event.getUserEmail(),
            "Share Your Feedback - Order #" + event.getOrderNumber(),
            htmlContent
        );
    }
}
