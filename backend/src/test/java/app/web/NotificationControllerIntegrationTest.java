package app.web;

import app.BaseIntegrationTest;
import app.notification.model.Notification;
import app.notification.model.NotificationType;
import app.notification.repository.NotificationRepository;
import app.testutil.TestDataFactory;
import app.user.model.AuthProvider;
import app.user.model.Role;
import app.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for NotificationController.
 * Tests notification management endpoints with a real database using Testcontainers.
 */
@DisplayName("Notification Controller Integration Tests")
class NotificationControllerIntegrationTest extends BaseIntegrationTest {

    private static final String NOTIFICATIONS_BASE_URL = "/api/notifications";

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private String authToken;
    private Notification testNotification;
    private Notification unreadNotification;

    @BeforeEach
    void setUp() {
        // Clean up previous test data
        notificationRepository.deleteAll();

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
                .build();
        testUser = userRepository.save(testUser);
        authToken = generateToken(testUser);

        // Create test notifications
        testNotification = Notification.builder()
                .userId(testUser.getId())
                .type(NotificationType.ORDER_PLACED)
                .title("Order Confirmed!")
                .message("Your order #ORD-123 has been placed")
                .actionUrl("/orders")
                .referenceId("ORD-123")
                .isRead(true)
                .readAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now().minusHours(1))
                .build();
        testNotification = notificationRepository.save(testNotification);

        unreadNotification = Notification.builder()
                .userId(testUser.getId())
                .type(NotificationType.ORDER_SHIPPED)
                .title("Order Shipped!")
                .message("Your order #ORD-456 has been shipped")
                .actionUrl("/orders")
                .referenceId("ORD-456")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
        unreadNotification = notificationRepository.save(unreadNotification);
    }

    @Nested
    @DisplayName("GET /api/notifications")
    class GetNotificationsTests {

        @Test
        @DisplayName("Should return paginated notifications for authenticated user")
        void shouldReturnPaginatedNotifications() throws Exception {
            mockMvc.perform(get(NOTIFICATIONS_BASE_URL)
                            .header("Authorization", "Bearer " + authToken)
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.notifications", hasSize(2)))
                    .andExpect(jsonPath("$.totalElements").value(2))
                    .andExpect(jsonPath("$.currentPage").value(0))
                    .andExpect(jsonPath("$.unreadCount").value(1));
        }

        @Test
        @DisplayName("Should return 401 for unauthenticated request")
        void shouldReturn401ForUnauthenticatedRequest() throws Exception {
            mockMvc.perform(get(NOTIFICATIONS_BASE_URL))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return notifications ordered by creation date descending")
        void shouldReturnNotificationsOrderedByDate() throws Exception {
            mockMvc.perform(get(NOTIFICATIONS_BASE_URL)
                            .header("Authorization", "Bearer " + authToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.notifications[0].title").value("Order Shipped!"))
                    .andExpect(jsonPath("$.notifications[1].title").value("Order Confirmed!"));
        }
    }

    @Nested
    @DisplayName("GET /api/notifications/unread")
    class GetUnreadNotificationsTests {

        @Test
        @DisplayName("Should return only unread notifications")
        void shouldReturnOnlyUnreadNotifications() throws Exception {
            mockMvc.perform(get(NOTIFICATIONS_BASE_URL + "/unread")
                            .header("Authorization", "Bearer " + authToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].title").value("Order Shipped!"))
                    .andExpect(jsonPath("$[0].isRead").value(false));
        }

        @Test
        @DisplayName("Should return empty list when no unread notifications")
        void shouldReturnEmptyListWhenNoUnread() throws Exception {
            // Mark all as read
            unreadNotification.setIsRead(true);
            unreadNotification.setReadAt(LocalDateTime.now());
            notificationRepository.save(unreadNotification);

            mockMvc.perform(get(NOTIFICATIONS_BASE_URL + "/unread")
                            .header("Authorization", "Bearer " + authToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }

    @Nested
    @DisplayName("GET /api/notifications/unread/count")
    class GetUnreadCountTests {

        @Test
        @DisplayName("Should return correct unread count")
        void shouldReturnCorrectUnreadCount() throws Exception {
            mockMvc.perform(get(NOTIFICATIONS_BASE_URL + "/unread/count")
                            .header("Authorization", "Bearer " + authToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.count").value(1));
        }

        @Test
        @DisplayName("Should return zero when no unread notifications")
        void shouldReturnZeroWhenNoUnread() throws Exception {
            unreadNotification.setIsRead(true);
            notificationRepository.save(unreadNotification);

            mockMvc.perform(get(NOTIFICATIONS_BASE_URL + "/unread/count")
                            .header("Authorization", "Bearer " + authToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.count").value(0));
        }
    }

    @Nested
    @DisplayName("PATCH /api/notifications/{id}/read")
    class MarkAsReadTests {

        @Test
        @DisplayName("Should mark notification as read")
        void shouldMarkNotificationAsRead() throws Exception {
            mockMvc.perform(patch(NOTIFICATIONS_BASE_URL + "/" + unreadNotification.getId() + "/read")
                            .header("Authorization", "Bearer " + authToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isRead").value(true))
                    .andExpect(jsonPath("$.readAt").isNotEmpty());

            // Verify in database
            Notification updated = notificationRepository.findById(unreadNotification.getId()).orElseThrow();
            org.assertj.core.api.Assertions.assertThat(updated.getIsRead()).isTrue();
        }

        @Test
        @DisplayName("Should return 404 for non-existent notification")
        void shouldReturn404ForNonExistentNotification() throws Exception {
            UUID nonExistentId = UUID.randomUUID();

            mockMvc.perform(patch(NOTIFICATIONS_BASE_URL + "/" + nonExistentId + "/read")
                            .header("Authorization", "Bearer " + authToken))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should not allow marking another user's notification as read")
        void shouldNotAllowMarkingOtherUsersNotification() throws Exception {
            // Create another user with their notification
            User otherUser = User.builder()
                    .email(TestDataFactory.generateUniqueEmail())
                    .password(passwordEncoder.encode("Password123!"))
                    .firstName("Other")
                    .lastName("User")
                    .role(Role.CUSTOMER)
                    .authProvider(AuthProvider.LOCAL)
                    .createdAt(LocalDateTime.now())
                    .build();
            otherUser = userRepository.save(otherUser);

            Notification otherUserNotification = Notification.builder()
                    .userId(otherUser.getId())
                    .type(NotificationType.PRICE_DROP)
                    .title("Price Drop!")
                    .message("Item is now on sale")
                    .isRead(false)
                    .createdAt(LocalDateTime.now())
                    .build();
            otherUserNotification = notificationRepository.save(otherUserNotification);

            mockMvc.perform(patch(NOTIFICATIONS_BASE_URL + "/" + otherUserNotification.getId() + "/read")
                            .header("Authorization", "Bearer " + authToken))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("POST /api/notifications/mark-all-read")
    class MarkAllAsReadTests {

        @Test
        @DisplayName("Should mark all notifications as read")
        void shouldMarkAllNotificationsAsRead() throws Exception {
            mockMvc.perform(post(NOTIFICATIONS_BASE_URL + "/mark-all-read")
                            .header("Authorization", "Bearer " + authToken))
                    .andExpect(status().isNoContent());

            // Verify unread count is now 0
            mockMvc.perform(get(NOTIFICATIONS_BASE_URL + "/unread/count")
                            .header("Authorization", "Bearer " + authToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.count").value(0));
        }
    }

    @Nested
    @DisplayName("DELETE /api/notifications/{id}")
    class DeleteNotificationTests {

        @Test
        @DisplayName("Should delete notification")
        void shouldDeleteNotification() throws Exception {
            mockMvc.perform(delete(NOTIFICATIONS_BASE_URL + "/" + testNotification.getId())
                            .header("Authorization", "Bearer " + authToken))
                    .andExpect(status().isNoContent());

            // Verify deleted
            org.assertj.core.api.Assertions.assertThat(
                    notificationRepository.findById(testNotification.getId())
            ).isEmpty();
        }

        @Test
        @DisplayName("Should return 404 when deleting non-existent notification")
        void shouldReturn404WhenDeletingNonExistent() throws Exception {
            UUID nonExistentId = UUID.randomUUID();

            mockMvc.perform(delete(NOTIFICATIONS_BASE_URL + "/" + nonExistentId)
                            .header("Authorization", "Bearer " + authToken))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should not allow deleting another user's notification")
        void shouldNotAllowDeletingOtherUsersNotification() throws Exception {
            User otherUser = User.builder()
                    .email(TestDataFactory.generateUniqueEmail())
                    .password(passwordEncoder.encode("Password123!"))
                    .firstName("Other")
                    .lastName("User")
                    .role(Role.CUSTOMER)
                    .authProvider(AuthProvider.LOCAL)
                    .createdAt(LocalDateTime.now())
                    .build();
            otherUser = userRepository.save(otherUser);

            Notification otherUserNotification = Notification.builder()
                    .userId(otherUser.getId())
                    .type(NotificationType.WELCOME)
                    .title("Welcome!")
                    .message("Thanks for joining")
                    .isRead(false)
                    .createdAt(LocalDateTime.now())
                    .build();
            otherUserNotification = notificationRepository.save(otherUserNotification);

            mockMvc.perform(delete(NOTIFICATIONS_BASE_URL + "/" + otherUserNotification.getId())
                            .header("Authorization", "Bearer " + authToken))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("User Isolation Tests")
    class UserIsolationTests {

        @Test
        @DisplayName("Should only return notifications belonging to authenticated user")
        void shouldOnlyReturnNotificationsBelongingToAuthenticatedUser() throws Exception {
            // Create another user with notifications
            User otherUser = User.builder()
                    .email(TestDataFactory.generateUniqueEmail())
                    .password(passwordEncoder.encode("Password123!"))
                    .firstName("Other")
                    .lastName("User")
                    .role(Role.CUSTOMER)
                    .authProvider(AuthProvider.LOCAL)
                    .createdAt(LocalDateTime.now())
                    .build();
            otherUser = userRepository.save(otherUser);

            Notification otherUserNotification = Notification.builder()
                    .userId(otherUser.getId())
                    .type(NotificationType.PROMOTIONAL)
                    .title("Special Offer!")
                    .message("50% off everything")
                    .isRead(false)
                    .createdAt(LocalDateTime.now())
                    .build();
            notificationRepository.save(otherUserNotification);

            // Verify original user only sees their own notifications
            mockMvc.perform(get(NOTIFICATIONS_BASE_URL)
                            .header("Authorization", "Bearer " + authToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.notifications", hasSize(2)))
                    .andExpect(jsonPath("$.notifications[*].title", 
                            not(hasItem("Special Offer!"))));
        }
    }
}
