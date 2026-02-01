package app.notification.handler.impl;

import app.notification.dto.NotificationPreferencesResponse;
import app.notification.event.AbandonedCartEvent;
import app.notification.handler.AbstractNotificationHandler;
import app.notification.service.EmailService;
import app.notification.service.EmailTemplateService;
import app.notification.service.NotificationPreferencesService;
import app.notification.service.NotificationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles AbandonedCartEvent - reminds customers about items left in their cart.
 */
@Component
public class AbandonedCartHandler extends AbstractNotificationHandler<AbandonedCartEvent> {

    private final EmailTemplateService emailTemplateService;
    
    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    public AbandonedCartHandler(NotificationService notificationService,
                                NotificationPreferencesService preferencesService,
                                EmailService emailService,
                                EmailTemplateService emailTemplateService) {
        super(notificationService, preferencesService, emailService);
        this.emailTemplateService = emailTemplateService;
    }

    @Override
    public Class<AbandonedCartEvent> getEventType() {
        return AbandonedCartEvent.class;
    }

    @Override
    protected String getEventDescription(AbandonedCartEvent event) {
        return String.format("Abandoned cart for user %s with %d items", 
            event.getUserEmail(), event.getItems().size());
    }

    @Override
    protected void processEvent(AbandonedCartEvent event) {
        NotificationPreferencesResponse prefs = getPreferences(event.getUserId());

        if (!isPreferenceEnabled(prefs.getPromotionalEmails(), "promotional emails (abandoned cart)", event.getUserEmail())) {
            return;
        }

        List<EmailTemplateService.CartItemDto> items = event.getItems().stream()
            .map(item -> new EmailTemplateService.CartItemDto(
                item.productName(), item.quantity(), item.price()))
            .collect(Collectors.toList());

        String cartUrl = frontendUrl + "/cart";
        String htmlContent = emailTemplateService.renderAbandonedCartEmail(
            event.getUserFirstName(), items, event.getCartTotal(), cartUrl);

        sendEmail(
            event.getUserEmail(),
            "You left items in your cart!",
            htmlContent
        );
    }
}
