package app.web;

import app.privacy.dto.PrivacySettingsResponse;
import app.privacy.dto.UpdatePrivacySettingsRequest;
import app.privacy.service.PrivacySettingsService;
import app.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/privacy-settings")
@RequiredArgsConstructor
@Slf4j
public class PrivacySettingsController {

    private final PrivacySettingsService privacySettingsService;

    @GetMapping
    public ResponseEntity<PrivacySettingsResponse> getPrivacySettings(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("Getting privacy settings for user: {}", userDetails.getId());
        PrivacySettingsResponse settings = privacySettingsService.getSettings(userDetails.getId());
        return ResponseEntity.ok(settings);
    }

    @PutMapping
    public ResponseEntity<PrivacySettingsResponse> updatePrivacySettings(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody UpdatePrivacySettingsRequest request) {

        log.info("Updating privacy settings for user: {}", userDetails.getId());
        PrivacySettingsResponse settings = privacySettingsService.updateSettings(
                userDetails.getId(),
                request
        );
        return ResponseEntity.ok(settings);
    }
}
