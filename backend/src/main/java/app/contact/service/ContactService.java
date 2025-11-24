package app.contact.service;

import app.contact.dto.ContactRequest;
import app.notification.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContactService {
    
    private final EmailService emailService;
    private final MessageSource messageSource;

    @Value("${app.email.admin}")
    private String adminEmail;
    
    @Value("${app.name}")
    private String appName;

    public void processContactRequest(ContactRequest request) {
        try {
            LocalDateTime timestamp = LocalDateTime.now();

            sendAdminNotification(request, timestamp);
            sendUserConfirmation(request, timestamp);

            log.info("Contact request processed successfully for: {}", request.getEmail());
        } catch (Exception e) {
            log.error("Error processing contact request: ", e);
            throw new RuntimeException("Failed to send email. Please try again later.");
        }
    }
    
    private void sendAdminNotification(ContactRequest request, LocalDateTime timestamp) {
        String readableSubject = getDisplaySubject(request.getSubject());

        String htmlContent = emailService.buildContactAdminNotificationEmail(
            request.getName(),
            request.getEmail(),
            readableSubject,
            request.getMessage(),
            appName,
            timestamp
        );
        
        emailService.sendEmail(
            adminEmail,
            "New Contact Form Submission: " + readableSubject,
            htmlContent
        );

        log.info("Admin notification sent to: {}", adminEmail);
    }
    
    private void sendUserConfirmation(ContactRequest request, LocalDateTime timestamp) {
        String readableSubject = getDisplaySubject(request.getSubject());

        String htmlContent = emailService.buildContactConfirmationEmail(
            request.getName(),
            readableSubject,
            appName,
            timestamp
        );

        emailService.sendEmail(
            request.getEmail(),
            "Thank you for contacting " + appName,
            htmlContent
        );

        log.info("Confirmation email sent to: {}", request.getEmail());
    }

    private String getDisplaySubject(String subject) {
        try {
            return messageSource.getMessage(
                "contact.subject." + subject,
                null,
                subject,
                Locale.getDefault()
            );
        } catch (Exception e) {
            log.warn("No display name found for subject: {}, using original value", subject);
            return subject;
        }
    }
}
