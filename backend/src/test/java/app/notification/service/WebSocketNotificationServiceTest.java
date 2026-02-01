package app.notification.service;

import app.notification.dto.NotificationDto;
import app.notification.dto.UnreadCountResponse;
import app.notification.model.NotificationType;
import app.user.model.Role;
import app.user.model.User;
import app.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebSocketNotificationServiceTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private UserRepository userRepository;

    private WebSocketNotificationService service;

    private UUID userId;
    private User testUser;
    private NotificationDto notification;

    @BeforeEach
    void setUp() {
        service = new WebSocketNotificationService(messagingTemplate, userRepository);

        userId = UUID.randomUUID();
        testUser = new User();
        testUser.setId(userId);
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setRole(Role.CUSTOMER);

        notification = NotificationDto.builder()
                .id(UUID.randomUUID())
                .type(NotificationType.ORDER_PLACED)
                .title("Order Confirmed!")
                .message("Your order has been placed successfully")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("Send Notification To User Tests")
    class SendNotificationToUserTests {

        @Test
        @DisplayName("Should send notification to user by email")
        void shouldSendNotificationToUserByEmail() {
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

            service.sendNotificationToUser(userId, notification);

            ArgumentCaptor<NotificationDto> notificationCaptor = ArgumentCaptor.forClass(NotificationDto.class);
            verify(messagingTemplate).convertAndSendToUser(
                    eq("test@example.com"),
                    eq("/queue/notifications"),
                    notificationCaptor.capture()
            );

            NotificationDto sent = notificationCaptor.getValue();
            assertThat(sent.getTitle()).isEqualTo("Order Confirmed!");
            assertThat(sent.getType()).isEqualTo(NotificationType.ORDER_PLACED);
        }

        @Test
        @DisplayName("Should not send notification when user not found")
        void shouldNotSendNotificationWhenUserNotFound() {
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            service.sendNotificationToUser(userId, notification);

            verify(messagingTemplate, never()).convertAndSendToUser(anyString(), anyString(), any());
        }

        @Test
        @DisplayName("Should handle messaging exception gracefully")
        void shouldHandleMessagingExceptionGracefully() {
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            doThrow(new RuntimeException("WebSocket error"))
                    .when(messagingTemplate).convertAndSendToUser(anyString(), anyString(), any());

            // Should not throw
            service.sendNotificationToUser(userId, notification);

            verify(messagingTemplate).convertAndSendToUser(anyString(), anyString(), any());
        }
    }

    @Nested
    @DisplayName("Send Unread Count To User Tests")
    class SendUnreadCountToUserTests {

        @Test
        @DisplayName("Should send unread count to user")
        void shouldSendUnreadCountToUser() {
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

            service.sendUnreadCountToUser(userId, 5L);

            ArgumentCaptor<UnreadCountResponse> captor = ArgumentCaptor.forClass(UnreadCountResponse.class);
            verify(messagingTemplate).convertAndSendToUser(
                    eq("test@example.com"),
                    eq("/queue/notifications/unread-count"),
                    captor.capture()
            );
            assertThat(captor.getValue().getCount()).isEqualTo(5L);
        }

        @Test
        @DisplayName("Should not send unread count when user not found")
        void shouldNotSendUnreadCountWhenUserNotFound() {
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            service.sendUnreadCountToUser(userId, 5L);

            verify(messagingTemplate, never()).convertAndSendToUser(anyString(), anyString(), any());
        }

        @Test
        @DisplayName("Should send zero unread count")
        void shouldSendZeroUnreadCount() {
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

            service.sendUnreadCountToUser(userId, 0L);

            ArgumentCaptor<UnreadCountResponse> captor = ArgumentCaptor.forClass(UnreadCountResponse.class);
            verify(messagingTemplate).convertAndSendToUser(
                    eq("test@example.com"),
                    eq("/queue/notifications/unread-count"),
                    captor.capture()
            );
            assertThat(captor.getValue().getCount()).isEqualTo(0L);
        }
    }

    @Nested
    @DisplayName("Multiple Users Tests")
    class MultipleUsersTests {

        @Test
        @DisplayName("Should send to different users with different emails")
        void shouldSendToDifferentUsersWithDifferentEmails() {
            UUID userId2 = UUID.randomUUID();
            User testUser2 = new User();
            testUser2.setId(userId2);
            testUser2.setEmail("another@example.com");
            testUser2.setFirstName("Another");
            testUser2.setLastName("User");
            testUser2.setRole(Role.CUSTOMER);

            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(userRepository.findById(userId2)).thenReturn(Optional.of(testUser2));

            NotificationDto notification2 = NotificationDto.builder()
                    .id(UUID.randomUUID())
                    .type(NotificationType.ORDER_SHIPPED)
                    .title("Order Shipped!")
                    .message("Your order is on its way")
                    .isRead(false)
                    .createdAt(LocalDateTime.now())
                    .build();

            service.sendNotificationToUser(userId, notification);
            service.sendNotificationToUser(userId2, notification2);

            verify(messagingTemplate).convertAndSendToUser(
                    eq("test@example.com"),
                    eq("/queue/notifications"),
                    eq(notification)
            );
            verify(messagingTemplate).convertAndSendToUser(
                    eq("another@example.com"),
                    eq("/queue/notifications"),
                    eq(notification2)
            );
        }
    }
}
