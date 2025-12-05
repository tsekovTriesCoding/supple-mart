package app.notification.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event published daily with business statistics for admin reporting.
 */
@Getter
public class DailyReportEvent extends ApplicationEvent {
    private final long totalOrders;
    private final long pendingOrders;
    private final long lowStockCount;

    public DailyReportEvent(Object source, long totalOrders, long pendingOrders, long lowStockCount) {
        super(source);
        this.totalOrders = totalOrders;
        this.pendingOrders = pendingOrders;
        this.lowStockCount = lowStockCount;
    }
}
