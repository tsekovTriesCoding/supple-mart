package app.notification.handler.impl;

import app.notification.dto.NotificationPreferencesResponse;
import app.notification.event.OrderDeliveredEvent;
import app.notification.handler.AbstractNotificationHandler;
import app.notification.model.NotificationType;
import app.notification.service.EmailService;
import app.notification.service.NotificationPreferencesService;
import app.notification.service.NotificationService;
import org.springframework.stereotype.Component;

/**
 * Handles OrderDeliveredEvent - sent when an order is delivered.
 */
@Component
public class OrderDeliveredHandler extends AbstractNotificationHandler<OrderDeliveredEvent> {

    public OrderDeliveredHandler(NotificationService notificationService,
                                 NotificationPreferencesService preferencesService,
                                 EmailService emailService) {
        super(notificationService, preferencesService, emailService);
    }

    @Override
    public Class<OrderDeliveredEvent> getEventType() {
        return OrderDeliveredEvent.class;
    }

    @Override
    protected String getEventDescription(OrderDeliveredEvent event) {
        return String.format("Order #%s delivered for user %s", event.getOrderNumber(), event.getUserEmail());
    }

    @Override
    protected void processEvent(OrderDeliveredEvent event) {
        NotificationPreferencesResponse prefs = getPreferences(event.getUserId());

        // Create in-app notification
        if (prefs.getShippingNotifications()) {
            createInAppNotification(
                event.getUserId(),
                NotificationType.ORDER_DELIVERED,
                "Order Delivered! ✅",
                "Your order #" + event.getOrderNumber() + " has been delivered. Enjoy your purchase!",
                "/orders",
                event.getOrderNumber()
            );
        }

        // Send email notification
        if (!isPreferenceEnabled(prefs.getShippingNotifications(), "shipping notifications", event.getUserEmail())) {
            return;
        }

        String htmlContent = emailService.buildOrderDeliveredEmail(
            event.getUserFirstName(),
            event.getOrderNumber()
        );

        sendEmail(
            event.getUserEmail(),
            "Your Order Has Been Delivered - #" + event.getOrderNumber(),
            htmlContent
        );
    }
}
