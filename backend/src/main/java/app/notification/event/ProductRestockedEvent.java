package app.notification.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.List;
import java.util.UUID;

@Getter
public class ProductRestockedEvent extends ApplicationEvent {
    private final String productName;
    private final List<UserNotificationData> interestedUsers;

    public ProductRestockedEvent(Object source, String productName, List<UserNotificationData> interestedUsers) {
        super(source);
        this.productName = productName;
        this.interestedUsers = interestedUsers;
    }

    public record UserNotificationData(UUID userId, String email, String firstName) {}
}
