package app.notification.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.List;

/**
 * Event published when products are detected to have low or zero stock.
 * This is an admin-only notification event.
 */
@Getter
public class LowStockAlertEvent extends ApplicationEvent {
    private final List<ProductStockData> lowStockProducts;
    private final List<ProductStockData> outOfStockProducts;

    public LowStockAlertEvent(Object source, List<ProductStockData> lowStockProducts,
                              List<ProductStockData> outOfStockProducts) {
        super(source);
        this.lowStockProducts = lowStockProducts;
        this.outOfStockProducts = outOfStockProducts;
    }

    public record ProductStockData(String name, int stockQuantity) {}
}
