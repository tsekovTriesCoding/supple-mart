package app.notification.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
public class OrderPlacedEvent extends ApplicationEvent {
    private final String orderNumber;
    private final UUID userId;
    private final String userEmail;
    private final String userFirstName;
    private final BigDecimal totalAmount;

    public OrderPlacedEvent(Object source, String orderNumber, UUID userId, String userEmail,
                           String userFirstName, BigDecimal totalAmount) {
        super(source);
        this.orderNumber = orderNumber;
        this.userId = userId;
        this.userEmail = userEmail;
        this.userFirstName = userFirstName;
        this.totalAmount = totalAmount;
    }
}
