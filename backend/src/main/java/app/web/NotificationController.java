package app.web;

import app.notification.dto.NotificationDto;
import app.notification.dto.NotificationPageResponse;
import app.notification.dto.UnreadCountResponse;
import app.notification.service.NotificationService;
import app.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Notifications", description = "Real-time notification management")
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "Get notifications", description = "Retrieve paginated list of user notifications")
    @ApiResponse(responseCode = "200", description = "Notifications retrieved successfully",
            content = @Content(schema = @Schema(implementation = NotificationPageResponse.class)))
    @GetMapping
    public ResponseEntity<NotificationPageResponse> getNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("Getting notifications for user: {}, page: {}, size: {}", userDetails.getId(), page, size);
        NotificationPageResponse response = notificationService.getNotifications(userDetails.getId(), page, size);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get unread notifications", description = "Retrieve all unread notifications")
    @ApiResponse(responseCode = "200", description = "Unread notifications retrieved successfully")
    @GetMapping("/unread")
    public ResponseEntity<List<NotificationDto>> getUnreadNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("Getting unread notifications for user: {}", userDetails.getId());
        List<NotificationDto> notifications = notificationService.getUnreadNotifications(userDetails.getId());
        return ResponseEntity.ok(notifications);
    }

    @Operation(summary = "Get unread count", description = "Get count of unread notifications")
    @ApiResponse(responseCode = "200", description = "Unread count retrieved successfully")
    @GetMapping("/unread/count")
    public ResponseEntity<UnreadCountResponse> getUnreadCount(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        long count = notificationService.getUnreadCount(userDetails.getId());
        return ResponseEntity.ok(UnreadCountResponse.builder().count(count).build());
    }

    @Operation(summary = "Mark notification as read", description = "Mark a specific notification as read")
    @ApiResponse(responseCode = "200", description = "Notification marked as read")
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<NotificationDto> markAsRead(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID notificationId) {

        log.info("Marking notification {} as read for user: {}", notificationId, userDetails.getId());
        NotificationDto notification = notificationService.markAsRead(notificationId, userDetails.getId());
        return ResponseEntity.ok(notification);
    }

    @Operation(summary = "Mark all as read", description = "Mark all notifications as read")
    @ApiResponse(responseCode = "204", description = "All notifications marked as read")
    @PostMapping("/mark-all-read")
    public ResponseEntity<Void> markAllAsRead(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("Marking all notifications as read for user: {}", userDetails.getId());
        notificationService.markAllAsRead(userDetails.getId());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Delete notification", description = "Delete a specific notification")
    @ApiResponse(responseCode = "204", description = "Notification deleted")
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> deleteNotification(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID notificationId) {

        log.info("Deleting notification {} for user: {}", notificationId, userDetails.getId());
        notificationService.deleteNotification(notificationId, userDetails.getId());
        return ResponseEntity.noContent().build();
    }
}
