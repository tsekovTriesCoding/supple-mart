package app.notification.listener;

import app.notification.dto.NotificationPreferencesResponse;
import app.notification.event.*;
import app.notification.service.EmailService;
import app.notification.service.NotificationPreferencesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

    private final EmailService emailService;
    private final NotificationPreferencesService notificationPreferencesService;

    @Async
    @EventListener
    public void handleOrderPlacedEvent(OrderPlacedEvent event) {
        log.info("Handling OrderPlacedEvent for order: {} and user: {}", event.getOrderNumber(), event.getUserEmail());

        try {
            NotificationPreferencesResponse prefs = notificationPreferencesService.getPreferences(event.getUserId());

            if (!prefs.getOrderUpdates()) {
                log.info("User {} has disabled order update notifications", event.getUserEmail());
                return;
            }

            String htmlContent = emailService.buildOrderConfirmationEmail(
                event.getUserFirstName(),
                event.getOrderNumber(),
                event.getTotalAmount()
            );

            emailService.sendEmail(
                event.getUserEmail(),
                "Order Confirmation - #" + event.getOrderNumber(),
                htmlContent
            );

            log.info("Order confirmation email sent to: {}", event.getUserEmail());
        } catch (Exception e) {
            log.error("Failed to handle OrderPlacedEvent for order: {}", event.getOrderNumber(), e);
        }
    }

    @Async
    @EventListener
    public void handleOrderShippedEvent(OrderShippedEvent event) {
        log.info("Handling OrderShippedEvent for order: {} and user: {}", event.getOrderNumber(), event.getUserEmail());

        try {
            NotificationPreferencesResponse prefs = notificationPreferencesService.getPreferences(event.getUserId());

            if (!prefs.getShippingNotifications()) {
                log.info("User {} has disabled shipping notifications", event.getUserEmail());
                return;
            }

            String htmlContent = emailService.buildOrderShippedEmail(
                event.getUserFirstName(),
                event.getOrderNumber(),
                event.getTrackingNumber()
            );

            emailService.sendEmail(
                event.getUserEmail(),
                "Your Order Has Been Shipped - #" + event.getOrderNumber(),
                htmlContent
            );

            log.info("Shipping notification sent to: {}", event.getUserEmail());
        } catch (Exception e) {
            log.error("Failed to handle OrderShippedEvent for order: {}", event.getOrderNumber(), e);
        }
    }

    @Async
    @EventListener
    public void handleOrderDeliveredEvent(OrderDeliveredEvent event) {
        log.info("Handling OrderDeliveredEvent for order: {} and user: {}", event.getOrderNumber(), event.getUserEmail());

        try {
            NotificationPreferencesResponse prefs = notificationPreferencesService.getPreferences(event.getUserId());

            if (!prefs.getShippingNotifications()) {
                log.info("User {} has disabled shipping notifications", event.getUserEmail());
                return;
            }

            String htmlContent = emailService.buildOrderDeliveredEmail(
                event.getUserFirstName(),
                event.getOrderNumber()
            );

            emailService.sendEmail(
                event.getUserEmail(),
                "Your Order Has Been Delivered - #" + event.getOrderNumber(),
                htmlContent
            );

            log.info("Delivery notification sent to: {}", event.getUserEmail());
        } catch (Exception e) {
            log.error("Failed to handle OrderDeliveredEvent for order: {}", event.getOrderNumber(), e);
        }
    }

    @Async
    @EventListener
    public void handlePriceDropEvent(PriceDropEvent event) {
        log.info("Handling PriceDropEvent for product: {}", event.getProductName());

        for (PriceDropEvent.UserNotificationData user : event.getInterestedUsers()) {
            try {
                NotificationPreferencesResponse prefs = notificationPreferencesService.getPreferences(user.userId());

                if (!prefs.getPriceDropAlerts()) {
                    log.info("User {} has disabled price drop alerts", user.email());
                    continue;
                }

                String htmlContent = emailService.buildPriceDropEmail(
                    user.firstName(),
                    event.getProductName(),
                    event.getOldPrice(),
                    event.getNewPrice()
                );

                emailService.sendEmail(
                    user.email(),
                    "Price Drop Alert: " + event.getProductName(),
                    htmlContent
                );

                log.info("Price drop alert sent to: {}", user.email());
            } catch (Exception e) {
                log.error("Failed to send price drop alert to user: {}", user.email(), e);
            }
        }
    }

    @Async
    @EventListener
    public void handleProductRestockedEvent(ProductRestockedEvent event) {
        log.info("Handling ProductRestockedEvent for product: {}", event.getProductName());

        for (ProductRestockedEvent.UserNotificationData user : event.getInterestedUsers()) {
            try {
                NotificationPreferencesResponse prefs = notificationPreferencesService.getPreferences(user.userId());

                if (!prefs.getBackInStockAlerts()) {
                    log.info("User {} has disabled back in stock alerts", user.email());
                    continue;
                }

                String htmlContent = emailService.buildRestockEmail(
                    user.firstName(),
                    event.getProductName()
                );

                emailService.sendEmail(
                    user.email(),
                    "Back in Stock: " + event.getProductName(),
                    htmlContent
                );

                log.info("Restock notification sent to: {}", user.email());
            } catch (Exception e) {
                log.error("Failed to send restock notification to user: {}", user.email(), e);
            }
        }
    }

    @Async
    @EventListener
    public void handleReviewReminderEvent(ReviewReminderEvent event) {
        log.info("Handling ReviewReminderEvent for order: {} and user: {}", event.getOrderNumber(), event.getUserEmail());

        try {
            NotificationPreferencesResponse prefs = notificationPreferencesService.getPreferences(event.getUserId());

            if (!prefs.getReviewReminders()) {
                log.info("User {} has disabled review reminders", event.getUserEmail());
                return;
            }

            String htmlContent = emailService.buildReviewReminderEmail(
                event.getUserFirstName(),
                event.getOrderNumber()
            );

            emailService.sendEmail(
                event.getUserEmail(),
                "Share Your Feedback - Order #" + event.getOrderNumber(),
                htmlContent
            );

            log.info("Review reminder sent to: {}", event.getUserEmail());
        } catch (Exception e) {
            log.error("Failed to handle ReviewReminderEvent for order: {}", event.getOrderNumber(), e);
        }
    }

    @Async
    @EventListener
    public void handleAccountSecurityEvent(AccountSecurityEvent event) {
        log.info("Handling AccountSecurityEvent for user: {} - Alert: {}", event.getUserEmail(), event.getAlertType());

        try {
            NotificationPreferencesResponse prefs = notificationPreferencesService.getPreferences(event.getUserId());

            if (!prefs.getAccountSecurityAlerts()) {
                log.info("User {} has disabled security alerts", event.getUserEmail());
                return;
            }

            String htmlContent = emailService.buildSecurityAlertEmail(
                event.getUserFirstName(),
                event.getAlertType(),
                event.getDetails()
            );

            emailService.sendEmail(
                event.getUserEmail(),
                "Security Alert: " + event.getAlertType(),
                htmlContent
            );

            log.info("Security alert sent to: {}", event.getUserEmail());
        } catch (Exception e) {
            log.error("Failed to handle AccountSecurityEvent for user: {}", event.getUserEmail(), e);
        }
    }

    @Async
    @EventListener
    public void handlePasswordResetEvent(PasswordResetEvent event) {
        log.info("Handling PasswordResetEvent for user: {}", event.getUserEmail());

        try {
            NotificationPreferencesResponse prefs = notificationPreferencesService.getPreferences(event.getUserId());

            if (!prefs.getPasswordResetEmails()) {
                log.info("User {} has disabled password reset emails", event.getUserEmail());
                return;
            }

            String htmlContent = emailService.buildPasswordResetEmail(
                event.getUserFirstName(),
                event.getResetToken()
            );

            emailService.sendEmail(
                event.getUserEmail(),
                "Password Reset Request",
                htmlContent
            );

            log.info("Password reset email sent to: {}", event.getUserEmail());
        } catch (Exception e) {
            log.error("Failed to handle PasswordResetEvent for user: {}", event.getUserEmail(), e);
        }
    }
}

