package app.notification.service;

import app.notification.dto.NotificationDto;
import app.notification.dto.UnreadCountResponse;
import app.user.model.User;
import app.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service for sending real-time notifications via WebSocket.
 * WebSocket user identity is based on email (from JWT), so userId must be resolved to email.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketNotificationService {

    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;

    private static final String USER_NOTIFICATION_DESTINATION = "/queue/notifications";
    private static final String USER_UNREAD_COUNT_DESTINATION = "/queue/notifications/unread-count";

    public void sendNotificationToUser(UUID userId, NotificationDto notification) {
        userRepository.findById(userId)
                .map(User::getEmail)
                .ifPresentOrElse(
                        userEmail -> sendToUser(userId, userEmail, notification),
                        () -> log.warn("Cannot send WebSocket notification - user not found: {}", userId)
                );
    }

    private void sendToUser(UUID userId, String userEmail, NotificationDto notification) {
        log.debug("Sending notification to user {} ({}) at destination {}", userId, userEmail, USER_NOTIFICATION_DESTINATION);

        try {
            messagingTemplate.convertAndSendToUser(
                    userEmail,
                    USER_NOTIFICATION_DESTINATION,
                    notification
            );
            log.info("Notification sent to user: {} ({}) - Type: {}", userId, userEmail, notification.getType());
        } catch (Exception e) {
            log.error("Failed to send notification to user: {} ({})", userId, userEmail, e);
        }
    }

    public void sendUnreadCountToUser(UUID userId, long unreadCount) {
        userRepository.findById(userId)
                .map(User::getEmail)
                .ifPresentOrElse(
                        userEmail -> sendUnreadCountToUser(userId, userEmail, unreadCount),
                        () -> log.warn("Cannot send WebSocket unread count - user not found: {}", userId)
                );
    }

    private void sendUnreadCountToUser(UUID userId, String userEmail, long unreadCount) {
        log.debug("Sending unread count {} to user {} ({}) at destination {}", unreadCount, userId, userEmail, USER_UNREAD_COUNT_DESTINATION);

        try {
            messagingTemplate.convertAndSendToUser(
                    userEmail,
                    USER_UNREAD_COUNT_DESTINATION,
                    UnreadCountResponse.of(unreadCount)
            );
        } catch (Exception e) {
            log.error("Failed to send unread count to user: {} ({})", userId, userEmail, e);
        }
    }
}
