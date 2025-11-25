package app.notification.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

@Getter
public class PasswordResetEvent extends ApplicationEvent {
    private final UUID userId;
    private final String userEmail;
    private final String userFirstName;
    private final String resetToken;

    public PasswordResetEvent(Object source, UUID userId, String userEmail, String userFirstName, String resetToken) {
        super(source);
        this.userId = userId;
        this.userEmail = userEmail;
        this.userFirstName = userFirstName;
        this.resetToken = resetToken;
    }
}
