package app.web;

import app.BaseIntegrationTest;
import app.notification.dto.UpdateNotificationPreferencesRequest;
import app.notification.model.NotificationPreferences;
import app.notification.repository.NotificationPreferencesRepository;
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
 * Integration tests for NotificationPreferencesController.
 * Tests notification preferences management endpoints with a real database using Testcontainers.
 */
@DisplayName("Notification Preferences Controller Integration Tests")
class NotificationPreferencesControllerIntegrationTest extends BaseIntegrationTest {

    private static final String NOTIFICATION_PREFS_BASE_URL = "/api/notification-preferences";

    @Autowired
    private NotificationPreferencesRepository notificationPreferencesRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private String authToken;

    @BeforeEach
    void setUp() {
        // Create test user
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
    @DisplayName("GET /api/notification-preferences")
    class GetNotificationPreferencesTests {

        @Test
        @DisplayName("Should return notification preferences for authenticated user")
        void getNotificationPreferences_AuthenticatedUser_ReturnsPreferences() throws Exception {
            // Create notification preferences for the user
            NotificationPreferences preferences = NotificationPreferences.builder()
                    .userId(testUser.getId())
                    .orderUpdates(true)
                    .shippingNotifications(true)
                    .promotionalEmails(false)
                    .newsletter(true)
                    .productRecommendations(true)
                    .priceDropAlerts(true)
                    .backInStockAlerts(false)
                    .accountSecurityAlerts(true)
                    .passwordResetEmails(true)
                    .reviewReminders(false)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            notificationPreferencesRepository.save(preferences);

            mockMvc.perform(get(NOTIFICATION_PREFS_BASE_URL)
                            .header("Authorization", bearerToken(authToken))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.orderUpdates").value(true))
                    .andExpect(jsonPath("$.shippingNotifications").value(true))
                    .andExpect(jsonPath("$.promotionalEmails").value(false))
                    .andExpect(jsonPath("$.newsletter").value(true))
                    .andExpect(jsonPath("$.productRecommendations").value(true))
                    .andExpect(jsonPath("$.priceDropAlerts").value(true))
                    .andExpect(jsonPath("$.backInStockAlerts").value(false))
                    .andExpect(jsonPath("$.accountSecurityAlerts").value(true))
                    .andExpect(jsonPath("$.passwordResetEmails").value(true))
                    .andExpect(jsonPath("$.reviewReminders").value(false));
        }

        @Test
        @DisplayName("Should return 401 without authentication")
        void getNotificationPreferences_WithoutAuth_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(get(NOTIFICATION_PREFS_BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("PUT /api/notification-preferences")
    class UpdateNotificationPreferencesTests {

        @BeforeEach
        void createPreferences() {
            // Create initial notification preferences
            NotificationPreferences preferences = NotificationPreferences.builder()
                    .userId(testUser.getId())
                    .orderUpdates(true)
                    .shippingNotifications(true)
                    .promotionalEmails(true)
                    .newsletter(true)
                    .productRecommendations(true)
                    .priceDropAlerts(true)
                    .backInStockAlerts(true)
                    .accountSecurityAlerts(true)
                    .passwordResetEmails(true)
                    .reviewReminders(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            notificationPreferencesRepository.save(preferences);
        }

        @Test
        @DisplayName("Should update notification preferences successfully")
        void updateNotificationPreferences_ValidData_ReturnsUpdatedPreferences() throws Exception {
            UpdateNotificationPreferencesRequest request = UpdateNotificationPreferencesRequest.builder()
                    .orderUpdates(true)
                    .shippingNotifications(true)
                    .promotionalEmails(false)
                    .newsletter(false)
                    .productRecommendations(true)
                    .priceDropAlerts(false)
                    .backInStockAlerts(true)
                    .accountSecurityAlerts(true)
                    .passwordResetEmails(true)
                    .reviewReminders(false)
                    .build();

            mockMvc.perform(put(NOTIFICATION_PREFS_BASE_URL)
                            .header("Authorization", bearerToken(authToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.orderUpdates").value(true))
                    .andExpect(jsonPath("$.promotionalEmails").value(false))
                    .andExpect(jsonPath("$.newsletter").value(false))
                    .andExpect(jsonPath("$.priceDropAlerts").value(false))
                    .andExpect(jsonPath("$.reviewReminders").value(false));
        }

        @Test
        @DisplayName("Should disable all marketing preferences")
        void updateNotificationPreferences_DisableAllMarketing_ReturnsUpdatedPreferences() throws Exception {
            UpdateNotificationPreferencesRequest request = UpdateNotificationPreferencesRequest.builder()
                    .orderUpdates(true)
                    .shippingNotifications(true)
                    .promotionalEmails(false)
                    .newsletter(false)
                    .productRecommendations(false)
                    .priceDropAlerts(false)
                    .backInStockAlerts(false)
                    .accountSecurityAlerts(true)
                    .passwordResetEmails(true)
                    .reviewReminders(false)
                    .build();

            mockMvc.perform(put(NOTIFICATION_PREFS_BASE_URL)
                            .header("Authorization", bearerToken(authToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.promotionalEmails").value(false))
                    .andExpect(jsonPath("$.newsletter").value(false))
                    .andExpect(jsonPath("$.productRecommendations").value(false))
                    .andExpect(jsonPath("$.priceDropAlerts").value(false))
                    .andExpect(jsonPath("$.backInStockAlerts").value(false));
        }

        @Test
        @DisplayName("Should return 400 for missing orderUpdates field")
        void updateNotificationPreferences_MissingOrderUpdates_ReturnsBadRequest() throws Exception {
            UpdateNotificationPreferencesRequest request = UpdateNotificationPreferencesRequest.builder()
                    .shippingNotifications(true)
                    .promotionalEmails(false)
                    .newsletter(false)
                    .productRecommendations(true)
                    .priceDropAlerts(false)
                    .backInStockAlerts(true)
                    .accountSecurityAlerts(true)
                    .passwordResetEmails(true)
                    .reviewReminders(false)
                    .build();

            mockMvc.perform(put(NOTIFICATION_PREFS_BASE_URL)
                            .header("Authorization", bearerToken(authToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 for missing required fields")
        void updateNotificationPreferences_MissingMultipleFields_ReturnsBadRequest() throws Exception {
            // Send a request with only some fields
            String incompleteJson = "{\"orderUpdates\": true, \"shippingNotifications\": true}";

            mockMvc.perform(put(NOTIFICATION_PREFS_BASE_URL)
                            .header("Authorization", bearerToken(authToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(incompleteJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 401 without authentication")
        void updateNotificationPreferences_WithoutAuth_ReturnsUnauthorized() throws Exception {
            UpdateNotificationPreferencesRequest request = UpdateNotificationPreferencesRequest.builder()
                    .orderUpdates(true)
                    .shippingNotifications(true)
                    .promotionalEmails(false)
                    .newsletter(false)
                    .productRecommendations(true)
                    .priceDropAlerts(false)
                    .backInStockAlerts(true)
                    .accountSecurityAlerts(true)
                    .passwordResetEmails(true)
                    .reviewReminders(false)
                    .build();

            mockMvc.perform(put(NOTIFICATION_PREFS_BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }
}
