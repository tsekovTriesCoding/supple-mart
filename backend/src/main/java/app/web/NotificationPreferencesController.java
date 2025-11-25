package app.web;

import app.notification.dto.NotificationPreferencesResponse;
import app.notification.dto.UpdateNotificationPreferencesRequest;
import app.notification.service.NotificationPreferencesService;
import app.security.CustomUserDetails;
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
public class NotificationPreferencesController {
    
    private final NotificationPreferencesService notificationPreferencesService;
    
    @GetMapping
    public ResponseEntity<NotificationPreferencesResponse> getNotificationPreferences(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        log.info("Getting notification preferences for user: {}", userDetails.getId());
        NotificationPreferencesResponse preferences = notificationPreferencesService.getPreferences(userDetails.getId());
        return ResponseEntity.ok(preferences);
    }
    
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
