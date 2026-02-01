package app.notification.handler.impl;

import app.notification.dto.NotificationPreferencesResponse;
import app.notification.event.OrderShippedEvent;
import app.notification.handler.AbstractNotificationHandler;
import app.notification.model.NotificationType;
import app.notification.service.EmailService;
import app.notification.service.NotificationPreferencesService;
import app.notification.service.NotificationService;
import org.springframework.stereotype.Component;

/**
 * Handles OrderShippedEvent - sent when an order is shipped.
 */
@Component
public class OrderShippedHandler extends AbstractNotificationHandler<OrderShippedEvent> {

    public OrderShippedHandler(NotificationService notificationService,
                               NotificationPreferencesService preferencesService,
                               EmailService emailService) {
        super(notificationService, preferencesService, emailService);
    }

    @Override
    public Class<OrderShippedEvent> getEventType() {
        return OrderShippedEvent.class;
    }

    @Override
    protected String getEventDescription(OrderShippedEvent event) {
        return String.format("Order #%s shipped for user %s", event.getOrderNumber(), event.getUserEmail());
    }

    @Override
    protected void processEvent(OrderShippedEvent event) {
        NotificationPreferencesResponse prefs = getPreferences(event.getUserId());

        // Create in-app notification
        if (prefs.getShippingNotifications()) {
            createInAppNotification(
                event.getUserId(),
                NotificationType.ORDER_SHIPPED,
                "Your Order is on its way! 📦",
                "Order #" + event.getOrderNumber() + " has been shipped. Track: " + event.getTrackingNumber(),
                "/orders",
                event.getOrderNumber()
            );
        }

        // Send email notification
        if (!isPreferenceEnabled(prefs.getShippingNotifications(), "shipping notifications", event.getUserEmail())) {
            return;
        }

        String htmlContent = emailService.buildOrderShippedEmail(
            event.getUserFirstName(),
            event.getOrderNumber(),
            event.getTrackingNumber()
        );

        sendEmail(
            event.getUserEmail(),
            "Your Order Has Been Shipped - #" + event.getOrderNumber(),
            htmlContent
        );
    }
}
