package app.notification.service;

import app.BaseIntegrationTest;
import app.notification.dto.NotificationPreferencesResponse;
import app.notification.dto.UpdateNotificationPreferencesRequest;
import app.notification.model.NotificationPreferences;
import app.notification.repository.NotificationPreferencesRepository;
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
 * Integration tests for NotificationPreferencesService.
 * Tests service layer functionality for managing notification preferences.
 */
class NotificationPreferencesServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private NotificationPreferencesService notificationPreferencesService;

    @Autowired
    private NotificationPreferencesRepository notificationPreferencesRepository;

    private User testUser;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setEmail("prefs-service-" + UUID.randomUUID() + "@example.com");
        testUser.setFirstName("Prefs");
        testUser.setLastName("Service");
        testUser.setPassword("hashedpassword");
        testUser.setRole(Role.CUSTOMER);
        testUser = userRepository.save(testUser);
        testUserId = testUser.getId();
    }

    /**
     * Helper method to create NotificationPreferences with all required fields set using builder
     */
    private NotificationPreferences createPreferencesWithDefaults(UUID userId) {
        return NotificationPreferences.builder()
                .userId(userId)
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
                .build();
    }

    @Nested
    @DisplayName("Get Preferences Tests")
    class GetPreferencesTests {

        @Test
        @DisplayName("Should create default preferences when none exist")
        void getPreferences_CreatesDefaultWhenNoneExist() {
            NotificationPreferencesResponse response = notificationPreferencesService.getPreferences(testUserId);

            assertThat(response).isNotNull();
            assertThat(response.getUserId()).isEqualTo(testUserId);
            assertThat(response.getOrderUpdates()).isTrue();
            assertThat(response.getShippingNotifications()).isTrue();
            assertThat(response.getAccountSecurityAlerts()).isTrue();
            assertThat(response.getPasswordResetEmails()).isTrue();
        }

        @Test
        @DisplayName("Should return existing preferences when they exist")
        void getPreferences_ReturnsExistingPreferences() {
            NotificationPreferences prefs = NotificationPreferences.builder()
                    .userId(testUserId)
                    .orderUpdates(false)
                    .shippingNotifications(true)
                    .priceDropAlerts(false)
                    .backInStockAlerts(true)
                    .reviewReminders(false)
                    .accountSecurityAlerts(true)
                    .passwordResetEmails(true)
                    .promotionalEmails(false)
                    .newsletter(false)
                    .productRecommendations(true)
                    .build();
            notificationPreferencesRepository.save(prefs);

            NotificationPreferencesResponse response = notificationPreferencesService.getPreferences(testUserId);

            assertThat(response).isNotNull();
            assertThat(response.getUserId()).isEqualTo(testUserId);
            assertThat(response.getOrderUpdates()).isFalse();
            assertThat(response.getShippingNotifications()).isTrue();
            assertThat(response.getPriceDropAlerts()).isFalse();
            assertThat(response.getBackInStockAlerts()).isTrue();
            assertThat(response.getReviewReminders()).isFalse();
            assertThat(response.getPromotionalEmails()).isFalse();
            assertThat(response.getNewsletter()).isFalse();
        }

        @Test
        @DisplayName("Should return preferences for different users independently")
        void getPreferences_ReturnsIndependentPreferencesForDifferentUsers() {
            User secondUser = new User();
            secondUser.setEmail("second-prefs-" + UUID.randomUUID() + "@example.com");
            secondUser.setFirstName("Second");
            secondUser.setLastName("PrefsUser");
            secondUser.setPassword("hashedpassword");
            secondUser.setRole(Role.CUSTOMER);
            secondUser = userRepository.save(secondUser);

            NotificationPreferences prefs1 = NotificationPreferences.builder()
                    .userId(testUserId)
                    .orderUpdates(true)
                    .shippingNotifications(true)
                    .newsletter(true)
                    .promotionalEmails(true)
                    .productRecommendations(true)
                    .priceDropAlerts(true)
                    .backInStockAlerts(true)
                    .accountSecurityAlerts(true)
                    .passwordResetEmails(true)
                    .reviewReminders(true)
                    .build();
            notificationPreferencesRepository.save(prefs1);

            NotificationPreferences prefs2 = NotificationPreferences.builder()
                    .userId(secondUser.getId())
                    .orderUpdates(false)
                    .shippingNotifications(true)
                    .newsletter(false)
                    .promotionalEmails(true)
                    .productRecommendations(true)
                    .priceDropAlerts(true)
                    .backInStockAlerts(true)
                    .accountSecurityAlerts(true)
                    .passwordResetEmails(true)
                    .reviewReminders(true)
                    .build();
            notificationPreferencesRepository.save(prefs2);

            NotificationPreferencesResponse response1 = notificationPreferencesService.getPreferences(testUserId);
            NotificationPreferencesResponse response2 = notificationPreferencesService.getPreferences(secondUser.getId());

            assertThat(response1.getOrderUpdates()).isTrue();
            assertThat(response1.getNewsletter()).isTrue();
            assertThat(response2.getOrderUpdates()).isFalse();
            assertThat(response2.getNewsletter()).isFalse();
        }
    }

    @Nested
    @DisplayName("Update Preferences Tests")
    class UpdatePreferencesTests {

        @Test
        @DisplayName("Should update existing preferences")
        void updatePreferences_UpdatesExistingPreferences() {
            NotificationPreferences prefs = createPreferencesWithDefaults(testUserId);
            notificationPreferencesRepository.save(prefs);

            UpdateNotificationPreferencesRequest request = new UpdateNotificationPreferencesRequest();
            request.setOrderUpdates(false);
            request.setPriceDropAlerts(false);

            NotificationPreferencesResponse response = notificationPreferencesService.updatePreferences(testUserId, request);

            assertThat(response.getOrderUpdates()).isFalse();
            assertThat(response.getPriceDropAlerts()).isFalse();
            assertThat(response.getShippingNotifications()).isTrue(); // unchanged

            NotificationPreferences updated = notificationPreferencesRepository.findByUserId(testUserId)
                    .orElseThrow();
            assertThat(updated.getOrderUpdates()).isFalse();
            assertThat(updated.getPriceDropAlerts()).isFalse();
        }

        @Test
        @DisplayName("Should create preferences when updating non-existent preferences")
        void updatePreferences_CreatesWhenNoneExist() {
            UpdateNotificationPreferencesRequest request = new UpdateNotificationPreferencesRequest();
            request.setOrderUpdates(true);
            request.setNewsletter(false);

            NotificationPreferencesResponse response = notificationPreferencesService.updatePreferences(testUserId, request);

            assertThat(response).isNotNull();
            assertThat(response.getUserId()).isEqualTo(testUserId);
            assertThat(response.getOrderUpdates()).isTrue();
            assertThat(response.getNewsletter()).isFalse();

            Optional<NotificationPreferences> savedPrefs = notificationPreferencesRepository.findByUserId(testUserId);
            assertThat(savedPrefs).isPresent();
        }

        @Test
        @DisplayName("Should handle partial updates correctly")
        void updatePreferences_HandlesPartialUpdates() {
            NotificationPreferences prefs = createPreferencesWithDefaults(testUserId);
            notificationPreferencesRepository.save(prefs);

            UpdateNotificationPreferencesRequest request = new UpdateNotificationPreferencesRequest();
            request.setPromotionalEmails(false);
            request.setNewsletter(false);

            NotificationPreferencesResponse response = notificationPreferencesService.updatePreferences(testUserId, request);

            assertThat(response.getPromotionalEmails()).isFalse();
            assertThat(response.getNewsletter()).isFalse();
            assertThat(response.getOrderUpdates()).isTrue();
            assertThat(response.getShippingNotifications()).isTrue();
            assertThat(response.getPriceDropAlerts()).isTrue();
        }

        @Test
        @DisplayName("Should update all preferences when all fields provided")
        void updatePreferences_UpdatesAllFieldsWhenProvided() {
            NotificationPreferences prefs = createPreferencesWithDefaults(testUserId);
            notificationPreferencesRepository.save(prefs);

            UpdateNotificationPreferencesRequest request = new UpdateNotificationPreferencesRequest();
            request.setOrderUpdates(false);
            request.setShippingNotifications(false);
            request.setPriceDropAlerts(false);
            request.setBackInStockAlerts(false);
            request.setReviewReminders(false);
            request.setAccountSecurityAlerts(false);
            request.setPasswordResetEmails(false);
            request.setPromotionalEmails(false);
            request.setNewsletter(false);
            request.setProductRecommendations(false);

            NotificationPreferencesResponse response = notificationPreferencesService.updatePreferences(testUserId, request);

            assertThat(response.getOrderUpdates()).isFalse();
            assertThat(response.getShippingNotifications()).isFalse();
            assertThat(response.getPriceDropAlerts()).isFalse();
            assertThat(response.getBackInStockAlerts()).isFalse();
            assertThat(response.getReviewReminders()).isFalse();
            assertThat(response.getAccountSecurityAlerts()).isFalse();
            assertThat(response.getPasswordResetEmails()).isFalse();
            assertThat(response.getPromotionalEmails()).isFalse();
            assertThat(response.getNewsletter()).isFalse();
            assertThat(response.getProductRecommendations()).isFalse();
        }
    }

    @Nested
    @DisplayName("Preference Isolation Tests")
    class PreferenceIsolationTests {

        @Test
        @DisplayName("Should not affect other users' preferences when updating")
        void updatePreferences_DoesNotAffectOtherUsers() {
            User secondUser = new User();
            secondUser.setEmail("isolation-" + UUID.randomUUID() + "@example.com");
            secondUser.setFirstName("Isolation");
            secondUser.setLastName("User");
            secondUser.setPassword("hashedpassword");
            secondUser.setRole(Role.CUSTOMER);
            secondUser = userRepository.save(secondUser);

            NotificationPreferences prefs1 = createPreferencesWithDefaults(testUserId);
            notificationPreferencesRepository.save(prefs1);

            NotificationPreferences prefs2 = createPreferencesWithDefaults(secondUser.getId());
            notificationPreferencesRepository.save(prefs2);

            UpdateNotificationPreferencesRequest request = new UpdateNotificationPreferencesRequest();
            request.setOrderUpdates(false);
            notificationPreferencesService.updatePreferences(testUserId, request);

            NotificationPreferencesResponse secondUserPrefs = notificationPreferencesService.getPreferences(secondUser.getId());
            assertThat(secondUserPrefs.getOrderUpdates()).isTrue();
        }
    }
}
