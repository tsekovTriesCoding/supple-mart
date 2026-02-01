package app.notification.handler;

/**
 * Strategy interface for handling notification events.
 * Each implementation handles a specific event type.
 * 
 * <p>Spring will auto-discover all implementations and register them
 * in the NotificationEventDispatcher.</p>
 * 
 * @param <E> The event type this handler processes
 */
public interface NotificationHandler<E> {

    /**
     * Returns the event class this handler supports.
     * Used by the dispatcher to route events to the correct handler.
     */
    Class<E> getEventType();

    /**
     * Process the notification event.
     * Implementations should:
     * 1. Check user preferences
     * 2. Create in-app notification (if applicable)
     * 3. Send email notification (if applicable)
     * 
     * @param event The event to process
     */
    void handle(E event);
}
