package app.exception;

public class NotificationAccessDeniedException extends RuntimeException {
    
    public NotificationAccessDeniedException(String message) {
        super(message);
    }

    public static NotificationAccessDeniedException forNotification(String notificationId) {
        return new NotificationAccessDeniedException(
                "Access denied: Notification " + notificationId + " does not belong to you"
        );
    }
}
