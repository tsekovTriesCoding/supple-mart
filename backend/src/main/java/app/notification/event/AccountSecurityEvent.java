package app.notification.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

@Getter
public class AccountSecurityEvent extends ApplicationEvent {
    private final UUID userId;
    private final String userEmail;
    private final String userFirstName;
    private final String alertType;
    private final String details;

    public AccountSecurityEvent(Object source, UUID userId, String userEmail, String userFirstName,
                               String alertType, String details) {
        super(source);
        this.userId = userId;
        this.userEmail = userEmail;
        this.userFirstName = userFirstName;
        this.alertType = alertType;
        this.details = details;
    }
}
