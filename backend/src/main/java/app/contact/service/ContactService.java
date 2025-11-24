package app.contact.service;

import app.contact.dto.ContactRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContactService {
    
    private final JavaMailSender mailSender;
    
    @Value("${app.email.admin}")
    private String adminEmail;
    
    @Value("${app.email.from}")
    private String fromEmail;
    
    @Value("${app.name}")
    private String appName;
    
    public void processContactRequest(ContactRequest request) {
        try {
            sendAdminNotification(request);
            sendUserConfirmation(request);
            
            log.info("Contact request processed successfully for: {}", request.getEmail());
        } catch (Exception e) {
            log.error("Error processing contact request: ", e);
            throw new RuntimeException("Failed to send email. Please try again later.");
        }
    }
    
    private void sendAdminNotification(ContactRequest request) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(adminEmail);
        message.setSubject("New Contact Form Submission: " + request.getSubject());
        
        String body = String.format("""
            New contact form submission received:
            
            From: %s
            Email: %s
            Subject: %s
            Date: %s
            
            Message:
            %s
            
            ---
            This is an automated message from %s Contact Form.
            """,
            request.getName(),
            request.getEmail(),
            request.getSubject(),
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
            request.getMessage(),
            appName
        );
        
        message.setText(body);
        mailSender.send(message);
        
        log.info("Admin notification sent to: {}", adminEmail);
    }
    
    private void sendUserConfirmation(ContactRequest request) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(request.getEmail());
        message.setSubject("Thank you for contacting " + appName);
        
        String body = String.format("""
            Dear %s,
            
            Thank you for reaching out to us. We have received your message and will get back to you as soon as possible.
            
            Your message details:
            Subject: %s
            Date: %s
            
            We typically respond within 24-48 hours. If your inquiry is urgent, please feel free to reach out to us directly.
            
            Best regards,
            The %s Team
            
            ---
            This is an automated confirmation email. Please do not reply to this email.
            """,
            request.getName(),
            request.getSubject(),
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
            appName
        );
        
        message.setText(body);
        mailSender.send(message);
        
        log.info("Confirmation email sent to: {}", request.getEmail());
    }
}
