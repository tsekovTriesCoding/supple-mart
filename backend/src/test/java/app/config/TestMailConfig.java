package app.config;

import jakarta.mail.internet.MimeMessage;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test configuration that provides mock beans for external services.
 * This prevents integration tests from making actual API calls to:
 * - Email services (SMTP)
 * - Payment gateways
 * - Other external APIs
 */
@TestConfiguration
public class TestMailConfig {

    /**
     * Mock JavaMailSender to prevent actual email sending during tests.
     * The @Primary annotation ensures this bean takes precedence over
     * the real JavaMailSender in the test context.
     */
    @Bean
    @Primary
    public JavaMailSender javaMailSender() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        // doNothing for send() - it's a void method, so no stubbing needed by default
        return mailSender;
    }
}

