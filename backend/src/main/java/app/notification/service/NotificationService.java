package app.notification.service;

import app.exception.NotificationAccessDeniedException;
import app.exception.ResourceNotFoundException;
import app.notification.dto.*;
import app.notification.mapper.NotificationMapper;
import app.notification.model.Notification;
import app.notification.model.NotificationType;
import app.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final WebSocketNotificationService webSocketNotificationService;

    @Transactional
    public NotificationDto createNotification(CreateNotificationRequest request) {
        log.info("Creating notification for user: {} of type: {}", request.getUserId(), request.getType());

        Notification notification = notificationMapper.toEntity(request);
        notification = notificationRepository.save(notification);

        NotificationDto dto = notificationMapper.toDto(notification);

        // Send real-time notification via WebSocket
        webSocketNotificationService.sendNotificationToUser(request.getUserId(), dto);

        return dto;
    }

    @Transactional
    public NotificationDto createAndSend(UUID userId, NotificationType type, String title, 
                                          String message, String actionUrl, String referenceId) {
        CreateNotificationRequest request = CreateNotificationRequest.builder()
                .userId(userId)
                .type(type)
                .title(title)
                .message(message)
                .actionUrl(actionUrl)
                .referenceId(referenceId)
                .build();

        return createNotification(request);
    }

    @Transactional(readOnly = true)
    public NotificationPageResponse getNotifications(UUID userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notificationPage = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        List<NotificationDto> notifications = notificationMapper.toDtoList(notificationPage.getContent());
        long unreadCount = notificationRepository.countByUserIdAndIsReadFalse(userId);
        Map<String, Long> unreadByType = getUnreadCountByType(userId);

        return NotificationPageResponse.builder()
                .notifications(notifications)
                .totalElements(notificationPage.getTotalElements())
                .totalPages(notificationPage.getTotalPages())
                .currentPage(page)
                .pageSize(size)
                .unreadCount(unreadCount)
                .unreadByType(unreadByType)
                .build();
    }

    @Transactional(readOnly = true)
    public List<NotificationDto> getUnreadNotifications(UUID userId) {
        List<Notification> notifications = notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        return notificationMapper.toDtoList(notifications);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(UUID userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getUnreadCountByType(UUID userId) {
        List<Object[]> results = notificationRepository.countUnreadByType(userId);
        return results.stream()
                .collect(Collectors.toMap(
                        row -> ((NotificationType) row[0]).name(),
                        row -> (Long) row[1]
                ));
    }

    @Transactional
    public NotificationDto markAsRead(UUID notificationId, UUID userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + notificationId));

        if (!notification.getUserId().equals(userId)) {
            throw NotificationAccessDeniedException.forNotification(notificationId.toString());
        }

        notification.markAsRead();
        notification = notificationRepository.save(notification);

        // Send updated unread count via WebSocket
        long unreadCount = notificationRepository.countByUserIdAndIsReadFalse(userId);
        webSocketNotificationService.sendUnreadCountToUser(userId, unreadCount);

        return notificationMapper.toDto(notification);
    }

    @Transactional
    public void markAllAsRead(UUID userId) {
        int updated = notificationRepository.markAllAsReadByUserId(userId, LocalDateTime.now());
        log.info("Marked {} notifications as read for user: {}", updated, userId);

        // Send updated unread count (0) via WebSocket
        webSocketNotificationService.sendUnreadCountToUser(userId, 0L);
    }

    @Transactional
    public void deleteNotification(UUID notificationId, UUID userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + notificationId));

        if (!notification.getUserId().equals(userId)) {
            throw NotificationAccessDeniedException.forNotification(notificationId.toString());
        }

        notificationRepository.delete(notification);
    }

    @Transactional
    public int cleanupOldNotifications(int daysToKeep) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
        int deleted = notificationRepository.deleteOlderThan(cutoffDate);
        log.info("Deleted {} old notifications older than {} days", deleted, daysToKeep);
        return deleted;
    }
}
