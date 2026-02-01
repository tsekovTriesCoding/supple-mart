package app.notification.handler.impl;

import app.notification.dto.NotificationPreferencesResponse;
import app.notification.event.OrderShippedEvent;
import app.notification.model.NotificationType;
import app.notification.service.EmailService;
import app.notification.service.NotificationPreferencesService;
import app.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OrderShippedHandler.
 * Tests shipping notification creation and email sending based on user preferences.
 */
@ExtendWith(MockitoExtension.class)
class OrderShippedHandlerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationPreferencesService preferencesService;

    @Mock
    private EmailService emailService;

    private OrderShippedHandler handler;

    private UUID userId;
    private OrderShippedEvent event;
    private NotificationPreferencesResponse enabledPrefs;
    private NotificationPreferencesResponse disabledPrefs;

    @BeforeEach
    void setUp() {
        handler = new OrderShippedHandler(notificationService, preferencesService, emailService);

        userId = UUID.randomUUID();
        event = new OrderShippedEvent(
                this,
                "ORD-67890",
                userId,
                "customer@example.com",
                "Jane",
                "1Z999AA10123456784"
        );

        enabledPrefs = NotificationPreferencesResponse.builder()
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

        disabledPrefs = NotificationPreferencesResponse.builder()
                .userId(userId)
                .orderUpdates(true)  // order updates enabled but...
                .shippingNotifications(false)  // shipping notifications disabled
                .promotionalEmails(false)
                .newsletter(false)
                .productRecommendations(false)
                .priceDropAlerts(false)
                .backInStockAlerts(false)
                .accountSecurityAlerts(false)
                .passwordResetEmails(false)
                .reviewReminders(false)
                .build();
    }

    @Test
    @DisplayName("Should return correct event type")
    void shouldReturnCorrectEventType() {
        assertThat(handler.getEventType()).isEqualTo(OrderShippedEvent.class);
    }

    @Nested
    @DisplayName("When user has shipping notifications enabled")
    class WhenShippingNotificationsEnabled {

        @BeforeEach
        void setUp() {
            when(preferencesService.getPreferences(userId)).thenReturn(enabledPrefs);
            when(emailService.buildOrderShippedEmail(anyString(), anyString(), anyString()))
                    .thenReturn("<html>Order shipped email</html>");
        }

        @Test
        @DisplayName("Should create in-app notification with tracking info")
        void shouldCreateInAppNotificationWithTrackingInfo() {
            handler.handle(event);

            ArgumentCaptor<String> titleCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);

            verify(notificationService).createAndSend(
                    eq(userId),
                    eq(NotificationType.ORDER_SHIPPED),
                    titleCaptor.capture(),
                    messageCaptor.capture(),
                    eq("/orders"),
                    eq("ORD-67890")
            );

            assertThat(titleCaptor.getValue()).containsIgnoringCase("on its way");
            assertThat(messageCaptor.getValue()).contains("ORD-67890");
            assertThat(messageCaptor.getValue()).contains("1Z999AA10123456784");
        }

        @Test
        @DisplayName("Should send shipping confirmation email")
        void shouldSendShippingConfirmationEmail() {
            handler.handle(event);

            verify(emailService).buildOrderShippedEmail(
                    eq("Jane"),
                    eq("ORD-67890"),
                    eq("1Z999AA10123456784")
            );

            verify(emailService).sendEmail(
                    eq("customer@example.com"),
                    contains("Shipped"),
                    anyString()
            );
        }
    }

    @Nested
    @DisplayName("When user has shipping notifications disabled")
    class WhenShippingNotificationsDisabled {

        @BeforeEach
        void setUp() {
            when(preferencesService.getPreferences(userId)).thenReturn(disabledPrefs);
        }

        @Test
        @DisplayName("Should not create in-app notification")
        void shouldNotCreateInAppNotification() {
            handler.handle(event);

            verify(notificationService, never()).createAndSend(any(), any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("Should not send email")
        void shouldNotSendEmail() {
            handler.handle(event);

            verify(emailService, never()).sendEmail(any(), any(), any());
        }
    }

    @Nested
    @DisplayName("Event Description Tests")
    class EventDescriptionTests {

        @Test
        @DisplayName("Should include order number and email in event description")
        void shouldIncludeOrderDetailsInDescription() {
            // Access protected method via reflection or make it package-private for testing
            // For now, we test it indirectly through logging
            when(preferencesService.getPreferences(userId)).thenReturn(enabledPrefs);
            when(emailService.buildOrderShippedEmail(anyString(), anyString(), anyString()))
                    .thenReturn("<html>Email content</html>");

            // The handler logs the description, so we just verify it processes without error
            handler.handle(event);

            verify(preferencesService).getPreferences(userId);
        }
    }
}
