package app.notification.handler.impl;

import app.notification.dto.NotificationPreferencesResponse;
import app.notification.event.ProductRestockedEvent;
import app.notification.handler.AbstractNotificationHandler;
import app.notification.model.NotificationType;
import app.notification.service.EmailService;
import app.notification.service.NotificationPreferencesService;
import app.notification.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Handles ProductRestockedEvent - sent when a product is back in stock.
 * Notifies all users who signed up for restock alerts.
 */
@Component
@Slf4j
public class ProductRestockedHandler extends AbstractNotificationHandler<ProductRestockedEvent> {

    public ProductRestockedHandler(NotificationService notificationService,
                                   NotificationPreferencesService preferencesService,
                                   EmailService emailService) {
        super(notificationService, preferencesService, emailService);
    }

    @Override
    public Class<ProductRestockedEvent> getEventType() {
        return ProductRestockedEvent.class;
    }

    @Override
    protected String getEventDescription(ProductRestockedEvent event) {
        return String.format("Product '%s' back in stock", event.getProductName());
    }

    @Override
    protected void processEvent(ProductRestockedEvent event) {
        // This event targets multiple users
        for (ProductRestockedEvent.UserNotificationData user : event.getInterestedUsers()) {
            processForUser(event, user);
        }
    }

    private void processForUser(ProductRestockedEvent event, ProductRestockedEvent.UserNotificationData user) {
        try {
            NotificationPreferencesResponse prefs = getPreferences(user.userId());

            if (!isPreferenceEnabled(prefs.getBackInStockAlerts(), "back in stock alerts", user.email())) {
                return;
            }

            // Create in-app notification
            createInAppNotification(
                user.userId(),
                NotificationType.BACK_IN_STOCK,
                "Back in Stock! 🔔",
                event.getProductName() + " is back in stock. Get it before it's gone!",
                "/products/" + event.getProductId(),
                event.getProductId().toString()
            );

            // Send email notification
            String htmlContent = emailService.buildRestockEmail(
                user.firstName(),
                event.getProductName()
            );

            sendEmail(
                user.email(),
                "Back in Stock: " + event.getProductName(),
                htmlContent
            );
        } catch (Exception e) {
            log.error("Failed to send restock notification to user: {}", user.email(), e);
        }
    }
}
