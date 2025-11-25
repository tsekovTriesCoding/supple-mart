package app.notification.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

@Getter
public class OrderShippedEvent extends ApplicationEvent {
    private final String orderNumber;
    private final UUID userId;
    private final String userEmail;
    private final String userFirstName;
    private final String trackingNumber;

    public OrderShippedEvent(Object source, String orderNumber, UUID userId, String userEmail,
                            String userFirstName, String trackingNumber) {
        super(source);
        this.orderNumber = orderNumber;
        this.userId = userId;
        this.userEmail = userEmail;
        this.userFirstName = userFirstName;
        this.trackingNumber = trackingNumber;
    }
}
