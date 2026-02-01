package app.notification.handler.impl;

import app.notification.event.LowStockAlertEvent;
import app.notification.handler.NotificationHandler;
import app.notification.service.EmailService;
import app.notification.service.EmailTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles LowStockAlertEvent - admin notification for inventory management.
 * This handler doesn't extend AbstractNotificationHandler because it's admin-only
 * and doesn't follow the typical user notification pattern.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LowStockAlertHandler implements NotificationHandler<LowStockAlertEvent> {

    private final EmailService emailService;
    private final EmailTemplateService emailTemplateService;

    @Value("${app.email.admin:admin@supplemart.com}")
    private String adminEmail;

    @Override
    public Class<LowStockAlertEvent> getEventType() {
        return LowStockAlertEvent.class;
    }

    @Override
    public void handle(LowStockAlertEvent event) {
        log.info("Handling LowStockAlertEvent: {} low stock, {} out of stock",
            event.getLowStockProducts().size(), event.getOutOfStockProducts().size());

        try {
            List<EmailTemplateService.ProductStockDto> lowStockDtos = event.getLowStockProducts().stream()
                .map(p -> new EmailTemplateService.ProductStockDto(p.name(), p.stockQuantity()))
                .collect(Collectors.toList());

            List<EmailTemplateService.ProductStockDto> outOfStockDtos = event.getOutOfStockProducts().stream()
                .map(p -> new EmailTemplateService.ProductStockDto(p.name(), p.stockQuantity()))
                .collect(Collectors.toList());

            String htmlContent = emailTemplateService.renderLowStockAlertEmail(lowStockDtos, outOfStockDtos);

            emailService.sendEmail(
                adminEmail,
                "Inventory Alert - Low Stock Products",
                htmlContent
            );

            log.info("Low stock alert sent to admin: {}", adminEmail);
        } catch (Exception e) {
            log.error("Failed to handle LowStockAlertEvent", e);
        }
    }
}
