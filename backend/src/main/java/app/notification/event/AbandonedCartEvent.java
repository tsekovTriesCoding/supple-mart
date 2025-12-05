package app.notification.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Event published when a cart is considered abandoned (not updated for a configured period).
 */
@Getter
public class AbandonedCartEvent extends ApplicationEvent {
    private final UUID userId;
    private final String userEmail;
    private final String userFirstName;
    private final List<CartItemData> items;
    private final BigDecimal cartTotal;

    public AbandonedCartEvent(Object source, UUID userId, String userEmail, String userFirstName,
                              List<CartItemData> items, BigDecimal cartTotal) {
        super(source);
        this.userId = userId;
        this.userEmail = userEmail;
        this.userFirstName = userFirstName;
        this.items = items;
        this.cartTotal = cartTotal;
    }

    public record CartItemData(String productName, int quantity, BigDecimal price) {}
}
