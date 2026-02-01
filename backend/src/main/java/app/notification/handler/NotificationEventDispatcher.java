package app.notification.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Central dispatcher that routes notification events to the appropriate handler.
 * 
 * <p>This class uses Spring's dependency injection to automatically discover
 * all NotificationHandler implementations and register them by event type.</p>
 * 
 * <p>Benefits of this approach:
 * <ul>
 *   <li>Open/Closed Principle: Add new handlers without modifying this class</li>
 *   <li>Single Responsibility: Each handler focuses on one event type</li>
 *   <li>Testability: Handlers can be unit tested in isolation</li>
 *   <li>Spring integration: Leverages Spring's event system and async processing</li>
 * </ul>
 * </p>
 */
@Component
@Slf4j
public class NotificationEventDispatcher {

    private final Map<Class<?>, NotificationHandler<?>> handlerMap = new HashMap<>();

    /**
     * Constructor injection - Spring automatically provides all NotificationHandler beans.
     * This is the recommended way to collect all implementations of an interface.
     */
    public NotificationEventDispatcher(List<NotificationHandler<?>> handlers) {
        for (NotificationHandler<?> handler : handlers) {
            handlerMap.put(handler.getEventType(), handler);
            log.info("Registered notification handler: {} for event type: {}", 
                    handler.getClass().getSimpleName(), 
                    handler.getEventType().getSimpleName());
        }
        log.info("NotificationEventDispatcher initialized with {} handlers", handlerMap.size());
    }

    /**
     * Main entry point for all notification events.
     * Spring's @EventListener will route all published events here.
     * The @Async annotation ensures non-blocking processing.
     * 
     * Note: Only processes events that have a registered handler,
     * silently ignores Spring internal events.
     */
    @Async
    @EventListener
    @SuppressWarnings("unchecked")
    public void handleEvent(Object event) {
        // Only process events from our notification.event package
        if (!event.getClass().getPackageName().startsWith("app.notification.event")) {
            return;
        }
        
        NotificationHandler<Object> handler = (NotificationHandler<Object>) handlerMap.get(event.getClass());
        
        if (handler != null) {
            log.debug("Dispatching {} to {}", event.getClass().getSimpleName(), handler.getClass().getSimpleName());
            handler.handle(event);
        } else {
            log.warn("No handler registered for event type: {}", event.getClass().getSimpleName());
        }
    }

    /**
     * Check if a handler is registered for the given event type.
     * Useful for testing and debugging.
     */
    public boolean hasHandlerFor(Class<?> eventType) {
        return handlerMap.containsKey(eventType);
    }

    /**
     * Get the number of registered handlers.
     */
    public int getHandlerCount() {
        return handlerMap.size();
    }
}
