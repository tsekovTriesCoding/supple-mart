package app.notification.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final EmailTemplateService emailTemplateService;

    @Value("${app.email.from}")
    private String fromEmail;

    public void sendEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send email to: {}", to, e);
        }
    }

    public String buildOrderConfirmationEmail(String customerName, String orderId, BigDecimal totalAmount) {
        return buildHtmlEmail(
            "Order Confirmation",
            String.format("Hi %s,", customerName),
            String.format("Thank you for your order! Your order #%s has been confirmed.", orderId),
            String.format("Order Total: $%.2f", totalAmount),
            "We'll send you another email when your order ships."
        );
    }

    public String buildOrderShippedEmail(String customerName, String orderId, String trackingNumber) {
        return buildHtmlEmail(
            "Order Shipped",
            String.format("Hi %s,", customerName),
            String.format("Great news! Your order #%s has been shipped.", orderId),
            String.format("Tracking Number: %s", trackingNumber),
            "You can track your package using the tracking number above."
        );
    }

    public String buildOrderDeliveredEmail(String customerName, String orderId) {
        return buildHtmlEmail(
            "Order Delivered",
            String.format("Hi %s,", customerName),
            String.format("Your order #%s has been delivered!", orderId),
            "We hope you enjoy your purchase!",
            "Please consider leaving a review to help other customers."
        );
    }

    public String buildPriceDropEmail(String customerName, String productName, Double oldPrice, Double newPrice) {
        double discount = ((oldPrice - newPrice) / oldPrice) * 100;
        return buildHtmlEmail(
            "Price Drop Alert",
            String.format("Hi %s,", customerName),
            String.format("Great news! The price of %s has dropped!", productName),
            String.format("Was: $%.2f | Now: $%.2f (%.0f%% off)", oldPrice, newPrice, discount),
            "Don't miss out on this deal!"
        );
    }

    public String buildRestockEmail(String customerName, String productName) {
        return buildHtmlEmail(
            "Back in Stock",
            String.format("Hi %s,", customerName),
            String.format("%s is back in stock!", productName),
            "The item you were waiting for is now available.",
            "Order now before it sells out again!"
        );
    }

    public String buildReviewReminderEmail(String customerName, String orderId) {
        return buildHtmlEmail(
            "Share Your Feedback",
            String.format("Hi %s,", customerName),
            String.format("How was your recent order #%s?", orderId),
            "We'd love to hear your thoughts!",
            "Your feedback helps us improve and helps other customers make informed decisions."
        );
    }

    public String buildSecurityAlertEmail(String customerName, String alertType, String details) {
        return buildHtmlEmail(
            "Security Alert",
            String.format("Hi %s,", customerName),
            String.format("Security Alert: %s", alertType),
            details,
            "If you didn't perform this action, please contact support immediately."
        );
    }

    public String buildPasswordResetEmail(String customerName, String resetToken) {
        return buildHtmlEmail(
            "Password Reset Request",
            String.format("Hi %s,", customerName),
            "We received a request to reset your password.",
            String.format("Your reset code: %s", resetToken),
            "This code will expire in 15 minutes. If you didn't request this, please ignore this email."
        );
    }

    public String buildContactAdminNotificationEmail(String customerName, String customerEmail, String subject,
                                                      String message, String appName, LocalDateTime timestamp) {
        return emailTemplateService.renderContactAdminNotification(
            customerName, customerEmail, subject, message, appName, timestamp
        );
    }

    public String buildContactConfirmationEmail(String customerName, String subject, String appName, LocalDateTime timestamp) {
        return emailTemplateService.renderContactConfirmation(
            customerName, subject, appName, timestamp
        );
    }

    private String buildHtmlEmail(String title, String greeting, String mainMessage, String detail, String footer) {
        return emailTemplateService.renderGeneralEmail(title, greeting, mainMessage, detail, footer);
    }
}
