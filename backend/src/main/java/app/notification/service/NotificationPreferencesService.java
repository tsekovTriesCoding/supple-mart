package app.notification.service;

import app.notification.dto.NotificationPreferencesResponse;
import app.notification.dto.UpdateNotificationPreferencesRequest;
import app.notification.mapper.NotificationPreferencesMapper;
import app.notification.model.NotificationPreferences;
import app.notification.repository.NotificationPreferencesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationPreferencesService {
    
    private final NotificationPreferencesRepository notificationPreferencesRepository;
    private final NotificationPreferencesMapper notificationPreferencesMapper;

    @Transactional(readOnly = true)
    public NotificationPreferencesResponse getPreferences(UUID userId) {
        log.info("Getting notification preferences for user: {}", userId);
        
        NotificationPreferences preferences = notificationPreferencesRepository.findByUserId(userId)
                .orElseGet(() -> {
                    NotificationPreferences defaultPrefs = notificationPreferencesMapper.createDefaultPreferences(userId);
                    return notificationPreferencesRepository.save(defaultPrefs);
                });

        return notificationPreferencesMapper.toResponse(preferences);
    }
    
    @Transactional
    public NotificationPreferencesResponse updatePreferences(UUID userId, UpdateNotificationPreferencesRequest request) {
        log.info("Updating notification preferences for user: {}", userId);
        
        NotificationPreferences preferences = notificationPreferencesRepository.findByUserId(userId)
                .orElseGet(() -> {
                    NotificationPreferences defaultPrefs = notificationPreferencesMapper.createDefaultPreferences(userId);
                    return notificationPreferencesRepository.save(defaultPrefs);
                });

        notificationPreferencesMapper.updatePreferencesFromRequest(preferences, request);

        NotificationPreferences savedPreferences = notificationPreferencesRepository.save(preferences);
        log.info("Notification preferences updated successfully for user: {}", userId);
        
        return notificationPreferencesMapper.toResponse(savedPreferences);
    }
}
