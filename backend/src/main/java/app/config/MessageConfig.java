package app.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;

/**
 * Configuration for message sources used in the application.
 * Supports externalized messages for contact subjects and future i18n.
 */
@Configuration
public class MessageConfig {

    /**
     * Message source for contact subject mappings.
     * Maps kebab-case values (e.g., "product-inquiry") to display names (e.g., "Product Inquiry").
     */
    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("contact-subjects");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setUseCodeAsDefaultMessage(true); // Fallback to code if key not found
        return messageSource;
    }
}

