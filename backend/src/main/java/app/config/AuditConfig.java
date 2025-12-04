package app.config;

import app.security.CustomUserDetails;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.envers.repository.config.EnableEnversRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
@EnableEnversRepositories(basePackages = "app")
public class AuditConfig {

    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated()) {
                return Optional.of("system");
            }
            
            Object principal = authentication.getPrincipal();
            
            if (principal instanceof CustomUserDetails userDetails) {
                return Optional.of(userDetails.getUsername());
            }
            
            if (principal instanceof String principalStr) {
                if ("anonymousUser".equals(principalStr)) {
                    return Optional.of("anonymous");
                }
                return Optional.of(principalStr);
            }
            
            return Optional.of("system");
        };
    }
}
