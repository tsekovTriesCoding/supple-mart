package app.web;

import app.BaseIntegrationTest;
import app.privacy.dto.UpdatePrivacySettingsRequest;
import app.privacy.model.PrivacySettings;
import app.privacy.repository.PrivacySettingsRepository;
import app.testutil.TestDataFactory;
import app.user.model.AuthProvider;
import app.user.model.Role;
import app.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for PrivacySettingsController.
 * Tests privacy settings management endpoints with a real database using Testcontainers.
 */
@DisplayName("Privacy Settings Controller Integration Tests")
class PrivacySettingsControllerIntegrationTest extends BaseIntegrationTest {

    private static final String PRIVACY_SETTINGS_BASE_URL = "/api/privacy-settings";

    @Autowired
    private PrivacySettingsRepository privacySettingsRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private String authToken;

    @BeforeEach
    void setUp() {
        String uniqueEmail = TestDataFactory.generateUniqueEmail();
        testUser = User.builder()
                .email(uniqueEmail)
                .password(passwordEncoder.encode("Password123!"))
                .firstName("Test")
                .lastName("User")
                .role(Role.CUSTOMER)
                .authProvider(AuthProvider.LOCAL)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        testUser = userRepository.save(testUser);
        authToken = generateToken(testUser);
    }

    @Nested
    @DisplayName("GET /api/privacy-settings")
    class GetPrivacySettingsTests {

        @Test
        @DisplayName("Should return privacy settings for authenticated user")
        void getPrivacySettings_AuthenticatedUser_ReturnsSettings() throws Exception {
            PrivacySettings settings = PrivacySettings.builder()
                    .userId(testUser.getId())
                    .showProfile(true)
                    .showActivity(false)
                    .showOnlineStatus(true)
                    .shareAnalytics(true)
                    .shareMarketing(false)
                    .shareThirdParty(false)
                    .searchable(true)
                    .allowMessages(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            privacySettingsRepository.save(settings);

            mockMvc.perform(get(PRIVACY_SETTINGS_BASE_URL)
                            .header("Authorization", bearerToken(authToken))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.showProfile").value(true))
                    .andExpect(jsonPath("$.showActivity").value(false))
                    .andExpect(jsonPath("$.showOnlineStatus").value(true))
                    .andExpect(jsonPath("$.shareAnalytics").value(true))
                    .andExpect(jsonPath("$.shareMarketing").value(false))
                    .andExpect(jsonPath("$.shareThirdParty").value(false))
                    .andExpect(jsonPath("$.searchable").value(true))
                    .andExpect(jsonPath("$.allowMessages").value(true));
        }

        @Test
        @DisplayName("Should return 401 without authentication")
        void getPrivacySettings_WithoutAuth_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(get(PRIVACY_SETTINGS_BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("PUT /api/privacy-settings")
    class UpdatePrivacySettingsTests {

        @BeforeEach
        void createPrivacySettings() {
            PrivacySettings settings = PrivacySettings.builder()
                    .userId(testUser.getId())
                    .showProfile(true)
                    .showActivity(true)
                    .showOnlineStatus(true)
                    .shareAnalytics(true)
                    .shareMarketing(true)
                    .shareThirdParty(true)
                    .searchable(true)
                    .allowMessages(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            privacySettingsRepository.save(settings);
        }

        @Test
        @DisplayName("Should update privacy settings successfully")
        void updatePrivacySettings_ValidData_ReturnsUpdatedSettings() throws Exception {
            UpdatePrivacySettingsRequest request = UpdatePrivacySettingsRequest.builder()
                    .showProfile(true)
                    .showActivity(false)
                    .showOnlineStatus(false)
                    .shareAnalytics(true)
                    .shareMarketing(false)
                    .shareThirdParty(false)
                    .searchable(true)
                    .allowMessages(false)
                    .build();

            mockMvc.perform(put(PRIVACY_SETTINGS_BASE_URL)
                            .header("Authorization", bearerToken(authToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.showProfile").value(true))
                    .andExpect(jsonPath("$.showActivity").value(false))
                    .andExpect(jsonPath("$.showOnlineStatus").value(false))
                    .andExpect(jsonPath("$.shareMarketing").value(false))
                    .andExpect(jsonPath("$.shareThirdParty").value(false))
                    .andExpect(jsonPath("$.allowMessages").value(false));
        }

        @Test
        @DisplayName("Should enable maximum privacy")
        void updatePrivacySettings_MaximumPrivacy_ReturnsUpdatedSettings() throws Exception {
            UpdatePrivacySettingsRequest request = UpdatePrivacySettingsRequest.builder()
                    .showProfile(false)
                    .showActivity(false)
                    .showOnlineStatus(false)
                    .shareAnalytics(false)
                    .shareMarketing(false)
                    .shareThirdParty(false)
                    .searchable(false)
                    .allowMessages(false)
                    .build();

            mockMvc.perform(put(PRIVACY_SETTINGS_BASE_URL)
                            .header("Authorization", bearerToken(authToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.showProfile").value(false))
                    .andExpect(jsonPath("$.showActivity").value(false))
                    .andExpect(jsonPath("$.showOnlineStatus").value(false))
                    .andExpect(jsonPath("$.shareAnalytics").value(false))
                    .andExpect(jsonPath("$.shareMarketing").value(false))
                    .andExpect(jsonPath("$.shareThirdParty").value(false))
                    .andExpect(jsonPath("$.searchable").value(false))
                    .andExpect(jsonPath("$.allowMessages").value(false));
        }

        @Test
        @DisplayName("Should enable all sharing options")
        void updatePrivacySettings_MinimumPrivacy_ReturnsUpdatedSettings() throws Exception {
            UpdatePrivacySettingsRequest request = UpdatePrivacySettingsRequest.builder()
                    .showProfile(true)
                    .showActivity(true)
                    .showOnlineStatus(true)
                    .shareAnalytics(true)
                    .shareMarketing(true)
                    .shareThirdParty(true)
                    .searchable(true)
                    .allowMessages(true)
                    .build();

            mockMvc.perform(put(PRIVACY_SETTINGS_BASE_URL)
                            .header("Authorization", bearerToken(authToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.showProfile").value(true))
                    .andExpect(jsonPath("$.showActivity").value(true))
                    .andExpect(jsonPath("$.showOnlineStatus").value(true))
                    .andExpect(jsonPath("$.shareAnalytics").value(true))
                    .andExpect(jsonPath("$.shareMarketing").value(true))
                    .andExpect(jsonPath("$.shareThirdParty").value(true))
                    .andExpect(jsonPath("$.searchable").value(true))
                    .andExpect(jsonPath("$.allowMessages").value(true));
        }

        @Test
        @DisplayName("Should update specific settings while keeping others unchanged")
        void updatePrivacySettings_SpecificFields_ReturnsUpdatedSettings() throws Exception {
            UpdatePrivacySettingsRequest request = UpdatePrivacySettingsRequest.builder()
                    .showProfile(false)
                    .showActivity(true)
                    .showOnlineStatus(true)
                    .shareAnalytics(true)
                    .shareMarketing(false)
                    .shareThirdParty(true)
                    .searchable(true)
                    .allowMessages(true)
                    .build();

            mockMvc.perform(put(PRIVACY_SETTINGS_BASE_URL)
                            .header("Authorization", bearerToken(authToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.showProfile").value(false))
                    .andExpect(jsonPath("$.shareMarketing").value(false));
        }

        @Test
        @DisplayName("Should return 401 without authentication")
        void updatePrivacySettings_WithoutAuth_ReturnsUnauthorized() throws Exception {
            UpdatePrivacySettingsRequest request = UpdatePrivacySettingsRequest.builder()
                    .showProfile(false)
                    .showActivity(false)
                    .showOnlineStatus(false)
                    .shareAnalytics(false)
                    .shareMarketing(false)
                    .shareThirdParty(false)
                    .searchable(false)
                    .allowMessages(false)
                    .build();

            mockMvc.perform(put(PRIVACY_SETTINGS_BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }
}
