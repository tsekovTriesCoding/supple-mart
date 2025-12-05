package app.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailTemplateService {

    private final TemplateEngine templateEngine;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    public String renderContactAdminNotification(String customerName, String customerEmail,
                                                   String subject, String message,
                                                   String appName, LocalDateTime timestamp) {
        Context context = new Context(Locale.getDefault());
        context.setVariable("customerName", customerName);
        context.setVariable("customerEmail", customerEmail);
        context.setVariable("subject", subject);
        context.setVariable("message", message.replace("\n", "<br>"));
        context.setVariable("appName", appName);
        context.setVariable("formattedDate", timestamp.format(DATE_FORMATTER));

        return templateEngine.process("emails/contact-admin-notification", context);
    }

    public String renderContactConfirmation(String customerName, String subject,
                                             String appName, LocalDateTime timestamp) {
        Context context = new Context(Locale.getDefault());
        context.setVariable("customerName", customerName);
        context.setVariable("subject", subject);
        context.setVariable("appName", appName);
        context.setVariable("formattedDate", timestamp.format(DATE_FORMATTER));

        return templateEngine.process("emails/contact-confirmation", context);
    }

    public String renderGeneralEmail(String title, String greeting, String mainMessage,
                                      String detail, String footer) {
        Context context = new Context(Locale.getDefault());
        context.setVariable("title", title);
        context.setVariable("greeting", greeting);
        context.setVariable("mainMessage", mainMessage);
        context.setVariable("detail", detail);
        context.setVariable("footer", footer);
        context.setVariable("currentTime", LocalDateTime.now().format(DATE_FORMATTER));

        return templateEngine.process("emails/general-email", context);
    }

    public String renderAbandonedCartEmail(String customerName, List<CartItemDto> items,
                                           BigDecimal cartTotal, String cartUrl) {
        Context context = new Context(Locale.getDefault());
        context.setVariable("customerName", customerName);
        context.setVariable("items", items);
        context.setVariable("cartTotal", cartTotal);
        context.setVariable("cartUrl", cartUrl);
        context.setVariable("currentTime", LocalDateTime.now().format(DATE_FORMATTER));

        return templateEngine.process("emails/abandoned-cart", context);
    }

    public String renderLowStockAlertEmail(List<ProductStockDto> lowStockProducts,
                                           List<ProductStockDto> outOfStockProducts) {
        Context context = new Context(Locale.getDefault());
        context.setVariable("lowStockProducts", lowStockProducts);
        context.setVariable("outOfStockProducts", outOfStockProducts);
        context.setVariable("lowStockCount", lowStockProducts.size());
        context.setVariable("outOfStockCount", outOfStockProducts.size());
        context.setVariable("currentTime", LocalDateTime.now().format(DATE_FORMATTER));

        return templateEngine.process("emails/low-stock-alert", context);
    }

    public String renderDailyReportEmail(long totalOrders, long pendingOrders, long lowStockCount) {
        Context context = new Context(Locale.getDefault());
        context.setVariable("totalOrders", totalOrders);
        context.setVariable("pendingOrders", pendingOrders);
        context.setVariable("lowStockCount", lowStockCount);
        context.setVariable("reportDate", LocalDateTime.now().format(DATE_FORMATTER));
        context.setVariable("currentTime", LocalDateTime.now().format(DATE_FORMATTER));

        return templateEngine.process("emails/daily-report", context);
    }

    public record CartItemDto(String productName, int quantity, BigDecimal price) {}
    public record ProductStockDto(String name, int stockQuantity) {}
}

