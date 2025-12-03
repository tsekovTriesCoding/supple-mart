package app.web;

import app.notification.dto.NotificationPreferencesResponse;
import app.notification.dto.UpdateNotificationPreferencesRequest;
import app.notification.service.NotificationPreferencesService;
import app.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notification-preferences")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Notification Preferences", description = "Manage email notification preferences")
public class NotificationPreferencesController {
    
    private final NotificationPreferencesService notificationPreferencesService;
    
    @Operation(summary = "Get notification preferences", description = "Retrieve the current user's notification preferences")
    @ApiResponse(responseCode = "200", description = "Preferences retrieved successfully",
            content = @Content(schema = @Schema(implementation = NotificationPreferencesResponse.class)))
    @GetMapping
    public ResponseEntity<NotificationPreferencesResponse> getNotificationPreferences(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        log.info("Getting notification preferences for user: {}", userDetails.getId());
        NotificationPreferencesResponse preferences = notificationPreferencesService.getPreferences(userDetails.getId());
        return ResponseEntity.ok(preferences);
    }
    
    @Operation(summary = "Update notification preferences", description = "Update the current user's notification preferences")
    @ApiResponse(responseCode = "200", description = "Preferences updated successfully")
    @PutMapping
    public ResponseEntity<NotificationPreferencesResponse> updateNotificationPreferences(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateNotificationPreferencesRequest request) {
        
        log.info("Updating notification preferences for user: {}", userDetails.getId());
        NotificationPreferencesResponse preferences = notificationPreferencesService.updatePreferences(
                userDetails.getId(), 
                request
        );
        return ResponseEntity.ok(preferences);
    }
}
