package app.notification.handler.impl;

import app.notification.dto.NotificationPreferencesResponse;
import app.notification.event.OrderPlacedEvent;
import app.notification.handler.AbstractNotificationHandler;
import app.notification.model.NotificationType;
import app.notification.service.EmailService;
import app.notification.service.NotificationPreferencesService;
import app.notification.service.NotificationService;
import org.springframework.stereotype.Component;

/**
 * Handles OrderPlacedEvent - sent when a customer successfully places an order.
 */
@Component
public class OrderPlacedHandler extends AbstractNotificationHandler<OrderPlacedEvent> {

    public OrderPlacedHandler(NotificationService notificationService,
                              NotificationPreferencesService preferencesService,
                              EmailService emailService) {
        super(notificationService, preferencesService, emailService);
    }

    @Override
    public Class<OrderPlacedEvent> getEventType() {
        return OrderPlacedEvent.class;
    }

    @Override
    protected String getEventDescription(OrderPlacedEvent event) {
        return String.format("Order #%s for user %s", event.getOrderNumber(), event.getUserEmail());
    }

    @Override
    protected void processEvent(OrderPlacedEvent event) {
        NotificationPreferencesResponse prefs = getPreferences(event.getUserId());

        // Create in-app notification
        if (prefs.getOrderUpdates()) {
            createInAppNotification(
                event.getUserId(),
                NotificationType.ORDER_PLACED,
                "Order Confirmed! 🎉",
                "Your order #" + event.getOrderNumber() + " has been placed successfully. Total: $" + event.getTotalAmount(),
                "/orders",
                event.getOrderNumber()
            );
        }

        // Send email notification
        if (!isPreferenceEnabled(prefs.getOrderUpdates(), "order update notifications", event.getUserEmail())) {
            return;
        }

        String htmlContent = emailService.buildOrderConfirmationEmail(
            event.getUserFirstName(),
            event.getOrderNumber(),
            event.getTotalAmount()
        );

        sendEmail(
            event.getUserEmail(),
            "Order Confirmation - #" + event.getOrderNumber(),
            htmlContent
        );
    }
}
