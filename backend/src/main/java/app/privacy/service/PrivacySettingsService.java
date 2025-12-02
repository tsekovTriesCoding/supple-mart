package app.privacy.service;

import app.privacy.dto.PrivacySettingsResponse;
import app.privacy.dto.UpdatePrivacySettingsRequest;
import app.privacy.mapper.PrivacySettingsMapper;
import app.privacy.model.PrivacySettings;
import app.privacy.repository.PrivacySettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PrivacySettingsService {

    private final PrivacySettingsRepository privacySettingsRepository;
    private final PrivacySettingsMapper privacySettingsMapper;

    @Transactional(readOnly = true)
    public PrivacySettingsResponse getSettings(UUID userId) {
        log.info("Getting privacy settings for user: {}", userId);

        PrivacySettings settings = privacySettingsRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultSettings(userId));

        return privacySettingsMapper.toResponse(settings);
    }

    @Transactional
    public PrivacySettingsResponse updateSettings(UUID userId, UpdatePrivacySettingsRequest request) {
        log.info("Updating privacy settings for user: {}", userId);

        PrivacySettings settings = privacySettingsRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultSettings(userId));

        privacySettingsMapper.updateSettingsFromRequest(settings, request);

        PrivacySettings savedSettings = privacySettingsRepository.save(settings);
        log.info("Privacy settings updated successfully for user: {}", userId);

        return privacySettingsMapper.toResponse(savedSettings);
    }

    private PrivacySettings createDefaultSettings(UUID userId) {
        log.info("Creating default privacy settings for user: {}", userId);

        PrivacySettings settings = privacySettingsMapper.createDefaultSettings(userId);
        return privacySettingsRepository.save(settings);
    }
}
