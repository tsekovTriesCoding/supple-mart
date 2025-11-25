package app.notification.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.List;
import java.util.UUID;

@Getter
public class PriceDropEvent extends ApplicationEvent {
    private final String productName;
    private final Double oldPrice;
    private final Double newPrice;
    private final List<UserNotificationData> interestedUsers;

    public PriceDropEvent(Object source, String productName, Double oldPrice, Double newPrice,
                         List<UserNotificationData> interestedUsers) {
        super(source);
        this.productName = productName;
        this.oldPrice = oldPrice;
        this.newPrice = newPrice;
        this.interestedUsers = interestedUsers;
    }

    public record UserNotificationData(UUID userId, String email, String firstName) {}
}
