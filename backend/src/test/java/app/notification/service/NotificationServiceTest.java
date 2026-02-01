package app.notification.service;

import app.exception.NotificationAccessDeniedException;
import app.exception.ResourceNotFoundException;
import app.notification.dto.CreateNotificationRequest;
import app.notification.dto.NotificationDto;
import app.notification.dto.NotificationPageResponse;
import app.notification.mapper.NotificationMapper;
import app.notification.model.Notification;
import app.notification.model.NotificationType;
import app.notification.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for NotificationService.
 * Tests core notification CRUD operations and WebSocket integration.
 */
@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationMapper notificationMapper;

    @Mock
    private WebSocketNotificationService webSocketNotificationService;

    private NotificationService notificationService;

    private UUID userId;
    private UUID notificationId;
    private Notification notification;
    private NotificationDto notificationDto;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(
                notificationRepository,
                notificationMapper,
                webSocketNotificationService
        );

        userId = UUID.randomUUID();
        notificationId = UUID.randomUUID();

        notification = new Notification();
        notification.setId(notificationId);
        notification.setUserId(userId);
        notification.setType(NotificationType.ORDER_PLACED);
        notification.setTitle("Order Confirmed!");
        notification.setMessage("Your order has been placed");
        notification.setIsRead(false);
        notification.setCreatedAt(LocalDateTime.now());

        notificationDto = NotificationDto.builder()
                .id(notificationId)
                .type(NotificationType.ORDER_PLACED)
                .title("Order Confirmed!")
                .message("Your order has been placed")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("Create Notification Tests")
    class CreateNotificationTests {

        @Test
        @DisplayName("Should create notification and send via WebSocket")
        void shouldCreateNotificationAndSendViaWebSocket() {
            CreateNotificationRequest request = CreateNotificationRequest.builder()
                    .userId(userId)
                    .type(NotificationType.ORDER_PLACED)
                    .title("Order Confirmed!")
                    .message("Your order has been placed")
                    .actionUrl("/orders")
                    .referenceId("ORD-123")
                    .build();

            when(notificationMapper.toEntity(request)).thenReturn(notification);
            when(notificationRepository.save(notification)).thenReturn(notification);
            when(notificationMapper.toDto(notification)).thenReturn(notificationDto);

            NotificationDto result = notificationService.createNotification(request);

            assertThat(result).isEqualTo(notificationDto);
            verify(notificationRepository).save(notification);
            verify(webSocketNotificationService).sendNotificationToUser(userId, notificationDto);
        }

        @Test
        @DisplayName("Should create notification using createAndSend convenience method")
        void shouldCreateNotificationUsingCreateAndSend() {
            when(notificationMapper.toEntity(any(CreateNotificationRequest.class))).thenReturn(notification);
            when(notificationRepository.save(notification)).thenReturn(notification);
            when(notificationMapper.toDto(notification)).thenReturn(notificationDto);

            NotificationDto result = notificationService.createAndSend(
                    userId,
                    NotificationType.ORDER_PLACED,
                    "Order Confirmed!",
                    "Your order has been placed",
                    "/orders",
                    "ORD-123"
            );

            assertThat(result).isEqualTo(notificationDto);
            
            ArgumentCaptor<CreateNotificationRequest> requestCaptor = 
                    ArgumentCaptor.forClass(CreateNotificationRequest.class);
            verify(notificationMapper).toEntity(requestCaptor.capture());
            
            CreateNotificationRequest captured = requestCaptor.getValue();
            assertThat(captured.getUserId()).isEqualTo(userId);
            assertThat(captured.getType()).isEqualTo(NotificationType.ORDER_PLACED);
        }
    }

    @Nested
    @DisplayName("Get Notifications Tests")
    class GetNotificationsTests {

        @Test
        @DisplayName("Should return paginated notifications")
        void shouldReturnPaginatedNotifications() {
            Pageable pageable = PageRequest.of(0, 20);
            Page<Notification> page = new PageImpl<>(List.of(notification), pageable, 1);

            when(notificationRepository.findByUserIdOrderByCreatedAtDesc(eq(userId), any(Pageable.class)))
                    .thenReturn(page);
            when(notificationMapper.toDtoList(List.of(notification))).thenReturn(List.of(notificationDto));
            when(notificationRepository.countByUserIdAndIsReadFalse(userId)).thenReturn(1L);
            when(notificationRepository.countUnreadByType(userId)).thenReturn(List.of());

            NotificationPageResponse response = notificationService.getNotifications(userId, 0, 20);

            assertThat(response.getNotifications()).hasSize(1);
            assertThat(response.getTotalElements()).isEqualTo(1);
            assertThat(response.getUnreadCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should return unread notifications")
        void shouldReturnUnreadNotifications() {
            when(notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId))
                    .thenReturn(List.of(notification));
            when(notificationMapper.toDtoList(List.of(notification))).thenReturn(List.of(notificationDto));

            List<NotificationDto> result = notificationService.getUnreadNotifications(userId);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getIsRead()).isFalse();
        }

        @Test
        @DisplayName("Should return unread count")
        void shouldReturnUnreadCount() {
            when(notificationRepository.countByUserIdAndIsReadFalse(userId)).thenReturn(5L);

            long count = notificationService.getUnreadCount(userId);

            assertThat(count).isEqualTo(5L);
        }
    }

    @Nested
    @DisplayName("Mark As Read Tests")
    class MarkAsReadTests {

        @Test
        @DisplayName("Should mark notification as read and send WebSocket update")
        void shouldMarkAsReadAndSendWebSocketUpdate() {
            when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));
            when(notificationRepository.save(notification)).thenReturn(notification);
            when(notificationMapper.toDto(notification)).thenReturn(notificationDto);
            when(notificationRepository.countByUserIdAndIsReadFalse(userId)).thenReturn(0L);

            notificationService.markAsRead(notificationId, userId);

            verify(notificationRepository).save(notification);
            verify(webSocketNotificationService).sendUnreadCountToUser(userId, 0L);
        }

        @Test
        @DisplayName("Should throw exception when notification not found")
        void shouldThrowExceptionWhenNotificationNotFound() {
            when(notificationRepository.findById(notificationId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> notificationService.markAsRead(notificationId, userId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining(notificationId.toString());
        }

        @Test
        @DisplayName("Should throw exception when notification belongs to different user")
        void shouldThrowExceptionWhenNotificationBelongsToDifferentUser() {
            UUID otherUserId = UUID.randomUUID();
            when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));

            assertThatThrownBy(() -> notificationService.markAsRead(notificationId, otherUserId))
                    .isInstanceOf(NotificationAccessDeniedException.class)
                    .hasMessageContaining(notificationId.toString());
        }
    }

    @Nested
    @DisplayName("Mark All As Read Tests")
    class MarkAllAsReadTests {

        @Test
        @DisplayName("Should mark all as read and send WebSocket update with zero count")
        void shouldMarkAllAsReadAndSendZeroCount() {
            when(notificationRepository.markAllAsReadByUserId(eq(userId), any(LocalDateTime.class)))
                    .thenReturn(5);

            notificationService.markAllAsRead(userId);

            verify(notificationRepository).markAllAsReadByUserId(eq(userId), any(LocalDateTime.class));
            verify(webSocketNotificationService).sendUnreadCountToUser(userId, 0L);
        }
    }

    @Nested
    @DisplayName("Delete Notification Tests")
    class DeleteNotificationTests {

        @Test
        @DisplayName("Should delete notification")
        void shouldDeleteNotification() {
            when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));

            notificationService.deleteNotification(notificationId, userId);

            verify(notificationRepository).delete(notification);
        }

        @Test
        @DisplayName("Should throw exception when deleting notification that belongs to different user")
        void shouldThrowExceptionWhenDeletingOtherUsersNotification() {
            UUID otherUserId = UUID.randomUUID();
            when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));

            assertThatThrownBy(() -> notificationService.deleteNotification(notificationId, otherUserId))
                    .isInstanceOf(NotificationAccessDeniedException.class)
                    .hasMessageContaining(notificationId.toString());
        }
    }

    @Nested
    @DisplayName("Cleanup Old Notifications Tests")
    class CleanupOldNotificationsTests {

        @Test
        @DisplayName("Should cleanup old notifications")
        void shouldCleanupOldNotifications() {
            when(notificationRepository.deleteOlderThan(any(LocalDateTime.class))).thenReturn(10);

            int deleted = notificationService.cleanupOldNotifications(30);

            assertThat(deleted).isEqualTo(10);
            verify(notificationRepository).deleteOlderThan(any(LocalDateTime.class));
        }
    }
}
