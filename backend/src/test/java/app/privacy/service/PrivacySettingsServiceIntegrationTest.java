package app.privacy.service;

import app.BaseIntegrationTest;
import app.privacy.dto.PrivacySettingsResponse;
import app.privacy.dto.UpdatePrivacySettingsRequest;
import app.privacy.model.PrivacySettings;
import app.privacy.repository.PrivacySettingsRepository;
import app.user.model.Role;
import app.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for PrivacySettingsService.
 * Tests service layer functionality for managing user privacy settings.
 */
class PrivacySettingsServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private PrivacySettingsService privacySettingsService;

    @Autowired
    private PrivacySettingsRepository privacySettingsRepository;

    private User testUser;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setEmail("privacy-service-" + UUID.randomUUID() + "@example.com");
        testUser.setFirstName("Privacy");
        testUser.setLastName("Service");
        testUser.setPassword("hashedpassword");
        testUser.setRole(Role.CUSTOMER);
        testUser = userRepository.save(testUser);
        testUserId = testUser.getId();
    }

    /**
     * Helper method to create PrivacySettings with all required fields
     */
    private PrivacySettings createSettingsWithDefaults(UUID userId) {
        return PrivacySettings.builder()
                .userId(userId)
                .showProfile(true)
                .showActivity(false)
                .showOnlineStatus(false)
                .shareAnalytics(false)
                .shareMarketing(false)
                .shareThirdParty(false)
                .searchable(true)
                .allowMessages(true)
                .build();
    }

    @Nested
    @DisplayName("Get Settings Tests")
    class GetSettingsTests {

        @Test
        @DisplayName("Should create default settings when none exist")
        void getSettings_CreatesDefaultWhenNoneExist() {
            PrivacySettingsResponse response = privacySettingsService.getSettings(testUserId);

            assertThat(response).isNotNull();
            assertThat(response.getUserId()).isEqualTo(testUserId);
            assertThat(response.getShowProfile()).isTrue();
            assertThat(response.getSearchable()).isTrue();
            assertThat(response.getAllowMessages()).isTrue();
        }

        @Test
        @DisplayName("Should return existing settings when they exist")
        void getSettings_ReturnsExistingSettings() {
            PrivacySettings settings = PrivacySettings.builder()
                    .userId(testUserId)
                    .showProfile(false)
                    .showActivity(false)
                    .showOnlineStatus(true)
                    .allowMessages(false)
                    .searchable(true)
                    .shareAnalytics(false)
                    .shareMarketing(false)
                    .shareThirdParty(false)
                    .build();
            privacySettingsRepository.save(settings);

            PrivacySettingsResponse response = privacySettingsService.getSettings(testUserId);

            assertThat(response).isNotNull();
            assertThat(response.getUserId()).isEqualTo(testUserId);
            assertThat(response.getShowProfile()).isFalse();
            assertThat(response.getShowActivity()).isFalse();
            assertThat(response.getShowOnlineStatus()).isTrue();
            assertThat(response.getAllowMessages()).isFalse();
            assertThat(response.getSearchable()).isTrue();
            assertThat(response.getShareAnalytics()).isFalse();
            assertThat(response.getShareMarketing()).isFalse();
            assertThat(response.getShareThirdParty()).isFalse();
        }

        @Test
        @DisplayName("Should return settings for different users independently")
        void getSettings_ReturnsIndependentSettingsForDifferentUsers() {
            User secondUser = new User();
            secondUser.setEmail("second-privacy-" + UUID.randomUUID() + "@example.com");
            secondUser.setFirstName("Second");
            secondUser.setLastName("PrivacyUser");
            secondUser.setPassword("hashedpassword");
            secondUser.setRole(Role.CUSTOMER);
            secondUser = userRepository.save(secondUser);

            PrivacySettings settings1 = PrivacySettings.builder()
                    .userId(testUserId)
                    .showProfile(true)
                    .shareMarketing(true)
                    .allowMessages(true)
                    .searchable(true)
                    .shareAnalytics(true)
                    .shareThirdParty(true)
                    .showActivity(true)
                    .showOnlineStatus(true)
                    .build();
            privacySettingsRepository.save(settings1);

            PrivacySettings settings2 = PrivacySettings.builder()
                    .userId(secondUser.getId())
                    .showProfile(false)
                    .shareMarketing(false)
                    .allowMessages(false)
                    .searchable(false)
                    .shareAnalytics(false)
                    .shareThirdParty(false)
                    .showActivity(false)
                    .showOnlineStatus(false)
                    .build();
            privacySettingsRepository.save(settings2);

            PrivacySettingsResponse response1 = privacySettingsService.getSettings(testUserId);
            PrivacySettingsResponse response2 = privacySettingsService.getSettings(secondUser.getId());

            assertThat(response1.getShowProfile()).isTrue();
            assertThat(response1.getShareMarketing()).isTrue();
            assertThat(response2.getShowProfile()).isFalse();
            assertThat(response2.getShareMarketing()).isFalse();
        }
    }

    @Nested
    @DisplayName("Update Settings Tests")
    class UpdateSettingsTests {

        @Test
        @DisplayName("Should update existing settings")
        void updateSettings_UpdatesExistingSettings() {
            PrivacySettings settings = createSettingsWithDefaults(testUserId);
            privacySettingsRepository.save(settings);

            UpdatePrivacySettingsRequest request = new UpdatePrivacySettingsRequest();
            request.setShowProfile(false);
            request.setShareMarketing(false);
            request.setShowActivity(true);
            request.setShowOnlineStatus(true);
            request.setAllowMessages(true);
            request.setSearchable(true);
            request.setShareAnalytics(true);
            request.setShareThirdParty(true);

            PrivacySettingsResponse response = privacySettingsService.updateSettings(testUserId, request);

            assertThat(response.getShowProfile()).isFalse();
            assertThat(response.getShareMarketing()).isFalse();
            assertThat(response.getShowActivity()).isTrue();

            PrivacySettings updated = privacySettingsRepository.findByUserId(testUserId)
                    .orElseThrow();
            assertThat(updated.getShowProfile()).isFalse();
            assertThat(updated.getShareMarketing()).isFalse();
        }

        @Test
        @DisplayName("Should create settings when updating non-existent settings")
        void updateSettings_CreatesWhenNoneExist() {
            // Given - no existing settings
            UpdatePrivacySettingsRequest request = new UpdatePrivacySettingsRequest();
            request.setShowProfile(true);
            request.setShareMarketing(false);
            request.setShowActivity(true);
            request.setShowOnlineStatus(true);
            request.setAllowMessages(true);
            request.setSearchable(true);
            request.setShareAnalytics(true);
            request.setShareThirdParty(true);

            PrivacySettingsResponse response = privacySettingsService.updateSettings(testUserId, request);

            assertThat(response).isNotNull();
            assertThat(response.getUserId()).isEqualTo(testUserId);
            assertThat(response.getShowProfile()).isTrue();
            assertThat(response.getShareMarketing()).isFalse();

            Optional<PrivacySettings> savedSettings = privacySettingsRepository.findByUserId(testUserId);
            assertThat(savedSettings).isPresent();
        }

        @Test
        @DisplayName("Should update all settings when all fields provided")
        void updateSettings_UpdatesAllFieldsWhenProvided() {
            PrivacySettings settings = PrivacySettings.builder()
                    .userId(testUserId)
                    .showProfile(true)
                    .showActivity(true)
                    .showOnlineStatus(true)
                    .allowMessages(true)
                    .searchable(true)
                    .shareAnalytics(true)
                    .shareMarketing(true)
                    .shareThirdParty(true)
                    .build();
            privacySettingsRepository.save(settings);

            UpdatePrivacySettingsRequest request = new UpdatePrivacySettingsRequest();
            request.setShowProfile(false);
            request.setShowActivity(false);
            request.setShowOnlineStatus(false);
            request.setAllowMessages(false);
            request.setSearchable(false);
            request.setShareAnalytics(false);
            request.setShareMarketing(false);
            request.setShareThirdParty(false);

            PrivacySettingsResponse response = privacySettingsService.updateSettings(testUserId, request);

            assertThat(response.getShowProfile()).isFalse();
            assertThat(response.getShowActivity()).isFalse();
            assertThat(response.getShowOnlineStatus()).isFalse();
            assertThat(response.getAllowMessages()).isFalse();
            assertThat(response.getSearchable()).isFalse();
            assertThat(response.getShareAnalytics()).isFalse();
            assertThat(response.getShareMarketing()).isFalse();
            assertThat(response.getShareThirdParty()).isFalse();
        }
    }

    @Nested
    @DisplayName("Settings Isolation Tests")
    class SettingsIsolationTests {

        @Test
        @DisplayName("Should not affect other users' settings when updating")
        void updateSettings_DoesNotAffectOtherUsers() {
            User secondUser = new User();
            secondUser.setEmail("isolation-privacy-" + UUID.randomUUID() + "@example.com");
            secondUser.setFirstName("Isolation");
            secondUser.setLastName("User");
            secondUser.setPassword("hashedpassword");
            secondUser.setRole(Role.CUSTOMER);
            secondUser = userRepository.save(secondUser);

            PrivacySettings settings1 = createSettingsWithDefaults(testUserId);
            settings1.setShowProfile(true);
            privacySettingsRepository.save(settings1);

            PrivacySettings settings2 = createSettingsWithDefaults(secondUser.getId());
            settings2.setShowProfile(true);
            privacySettingsRepository.save(settings2);

            UpdatePrivacySettingsRequest request = new UpdatePrivacySettingsRequest();
            request.setShowProfile(false);
            request.setShowActivity(true);
            request.setShowOnlineStatus(true);
            request.setAllowMessages(true);
            request.setSearchable(true);
            request.setShareAnalytics(true);
            request.setShareMarketing(true);
            request.setShareThirdParty(true);
            privacySettingsService.updateSettings(testUserId, request);

            PrivacySettingsResponse secondUserSettings = privacySettingsService.getSettings(secondUser.getId());
            assertThat(secondUserSettings.getShowProfile()).isTrue();
        }
    }
}
