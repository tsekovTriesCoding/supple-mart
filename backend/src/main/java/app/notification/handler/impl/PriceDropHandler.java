package app.notification.handler.impl;

import app.notification.dto.NotificationPreferencesResponse;
import app.notification.event.PriceDropEvent;
import app.notification.handler.AbstractNotificationHandler;
import app.notification.model.NotificationType;
import app.notification.service.EmailService;
import app.notification.service.NotificationPreferencesService;
import app.notification.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Handles PriceDropEvent - sent when a product's price drops.
 * Notifies all interested users who have this product in their wishlist.
 */
@Component
@Slf4j
public class PriceDropHandler extends AbstractNotificationHandler<PriceDropEvent> {

    public PriceDropHandler(NotificationService notificationService,
                            NotificationPreferencesService preferencesService,
                            EmailService emailService) {
        super(notificationService, preferencesService, emailService);
    }

    @Override
    public Class<PriceDropEvent> getEventType() {
        return PriceDropEvent.class;
    }

    @Override
    protected String getEventDescription(PriceDropEvent event) {
        return String.format("Product '%s' price dropped from $%s to $%s", 
            event.getProductName(), event.getOldPrice(), event.getNewPrice());
    }

    @Override
    protected void processEvent(PriceDropEvent event) {
        // This event targets multiple users
        for (PriceDropEvent.UserNotificationData user : event.getInterestedUsers()) {
            processForUser(event, user);
        }
    }

    private void processForUser(PriceDropEvent event, PriceDropEvent.UserNotificationData user) {
        try {
            NotificationPreferencesResponse prefs = getPreferences(user.userId());

            if (!isPreferenceEnabled(prefs.getPriceDropAlerts(), "price drop alerts", user.email())) {
                return;
            }

            // Create in-app notification
            createInAppNotification(
                user.userId(),
                NotificationType.PRICE_DROP,
                "Price Drop Alert! 💰",
                event.getProductName() + " dropped from $" + event.getOldPrice() + " to $" + event.getNewPrice(),
                "/products/" + event.getProductId(),
                event.getProductId().toString()
            );

            // Send email notification
            String htmlContent = emailService.buildPriceDropEmail(
                user.firstName(),
                event.getProductName(),
                event.getOldPrice(),
                event.getNewPrice()
            );

            sendEmail(
                user.email(),
                "Price Drop Alert: " + event.getProductName(),
                htmlContent
            );
        } catch (Exception e) {
            log.error("Failed to send price drop alert to user: {}", user.email(), e);
        }
    }
}
