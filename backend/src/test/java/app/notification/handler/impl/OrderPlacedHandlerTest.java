package app.notification.handler.impl;

import app.notification.dto.NotificationPreferencesResponse;
import app.notification.event.OrderPlacedEvent;
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

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OrderPlacedHandler.
 * Tests notification creation and email sending based on user preferences.
 */
@ExtendWith(MockitoExtension.class)
class OrderPlacedHandlerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationPreferencesService preferencesService;

    @Mock
    private EmailService emailService;

    private OrderPlacedHandler handler;

    private UUID userId;
    private OrderPlacedEvent event;
    private NotificationPreferencesResponse enabledPrefs;
    private NotificationPreferencesResponse disabledPrefs;

    @BeforeEach
    void setUp() {
        handler = new OrderPlacedHandler(notificationService, preferencesService, emailService);

        userId = UUID.randomUUID();
        event = new OrderPlacedEvent(
                this,
                "ORD-12345",
                userId,
                "customer@example.com",
                "John",
                new BigDecimal("149.99")
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
                .orderUpdates(false)
                .shippingNotifications(false)
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
        assertThat(handler.getEventType()).isEqualTo(OrderPlacedEvent.class);
    }

    @Nested
    @DisplayName("When user has order updates enabled")
    class WhenOrderUpdatesEnabled {

        @BeforeEach
        void setUp() {
            when(preferencesService.getPreferences(userId)).thenReturn(enabledPrefs);
            when(emailService.buildOrderConfirmationEmail(anyString(), anyString(), any(BigDecimal.class)))
                    .thenReturn("<html>Order confirmation email</html>");
        }

        @Test
        @DisplayName("Should create in-app notification")
        void shouldCreateInAppNotification() {
            handler.handle(event);

            ArgumentCaptor<String> titleCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);

            verify(notificationService).createAndSend(
                    eq(userId),
                    eq(NotificationType.ORDER_PLACED),
                    titleCaptor.capture(),
                    messageCaptor.capture(),
                    eq("/orders"),
                    eq("ORD-12345")
            );

            assertThat(titleCaptor.getValue()).contains("Order Confirmed");
            assertThat(messageCaptor.getValue()).contains("ORD-12345");
            assertThat(messageCaptor.getValue()).contains("149.99");
        }

        @Test
        @DisplayName("Should send confirmation email")
        void shouldSendConfirmationEmail() {
            handler.handle(event);

            verify(emailService).buildOrderConfirmationEmail(
                    eq("John"),
                    eq("ORD-12345"),
                    eq(new BigDecimal("149.99"))
            );

            verify(emailService).sendEmail(
                    eq("customer@example.com"),
                    contains("Order Confirmation"),
                    anyString()
            );
        }
    }

    @Nested
    @DisplayName("When user has order updates disabled")
    class WhenOrderUpdatesDisabled {

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
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle exception gracefully without propagating")
        void shouldHandleExceptionGracefully() {
            when(preferencesService.getPreferences(userId))
                    .thenThrow(new RuntimeException("Database error"));

            // Should not throw
            handler.handle(event);

            verify(emailService, never()).sendEmail(any(), any(), any());
        }
    }
}
