package app.web;

import app.privacy.dto.PrivacySettingsResponse;
import app.privacy.dto.UpdatePrivacySettingsRequest;
import app.privacy.service.PrivacySettingsService;
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

@RestController
@RequestMapping("/api/privacy-settings")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Privacy Settings", description = "Manage user privacy settings")
public class PrivacySettingsController {

    private final PrivacySettingsService privacySettingsService;

    @Operation(summary = "Get privacy settings", description = "Retrieve the current user's privacy settings")
    @ApiResponse(responseCode = "200", description = "Settings retrieved successfully",
            content = @Content(schema = @Schema(implementation = PrivacySettingsResponse.class)))
    @GetMapping
    public ResponseEntity<PrivacySettingsResponse> getPrivacySettings(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("Getting privacy settings for user: {}", userDetails.getId());
        PrivacySettingsResponse settings = privacySettingsService.getSettings(userDetails.getId());
        return ResponseEntity.ok(settings);
    }

    @Operation(summary = "Update privacy settings", description = "Update the current user's privacy settings")
    @ApiResponse(responseCode = "200", description = "Settings updated successfully")
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
