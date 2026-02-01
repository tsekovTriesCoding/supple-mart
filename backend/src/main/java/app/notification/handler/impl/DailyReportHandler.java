package app.notification.handler.impl;

import app.notification.event.DailyReportEvent;
import app.notification.handler.NotificationHandler;
import app.notification.service.EmailService;
import app.notification.service.EmailTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Handles DailyReportEvent - admin notification with daily business metrics.
 * This handler doesn't extend AbstractNotificationHandler because it's admin-only
 * and doesn't follow the typical user notification pattern.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DailyReportHandler implements NotificationHandler<DailyReportEvent> {

    private final EmailService emailService;
    private final EmailTemplateService emailTemplateService;

    @Value("${app.email.admin:admin@supplemart.com}")
    private String adminEmail;

    @Override
    public Class<DailyReportEvent> getEventType() {
        return DailyReportEvent.class;
    }

    @Override
    public void handle(DailyReportEvent event) {
        log.info("Handling DailyReportEvent: {} total orders, {} pending, {} low stock",
            event.getTotalOrders(), event.getPendingOrders(), event.getLowStockCount());

        try {
            String htmlContent = emailTemplateService.renderDailyReportEmail(
                event.getTotalOrders(), event.getPendingOrders(), event.getLowStockCount());

            emailService.sendEmail(
                adminEmail,
                "Daily Business Report - SuppleMart",
                htmlContent
            );

            log.info("Daily report sent to admin: {}", adminEmail);
        } catch (Exception e) {
            log.error("Failed to handle DailyReportEvent", e);
        }
    }
}
