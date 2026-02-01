package app.notification.handler.impl;

import app.notification.dto.NotificationPreferencesResponse;
import app.notification.event.OrderDeliveredEvent;
import app.notification.model.NotificationType;
import app.notification.service.EmailService;
import app.notification.service.NotificationPreferencesService;
import app.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderDeliveredHandlerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationPreferencesService preferencesService;

    @Mock
    private EmailService emailService;

    private OrderDeliveredHandler handler;

    private UUID userId;
    private OrderDeliveredEvent event;
    private NotificationPreferencesResponse enabledPrefs;
    private NotificationPreferencesResponse disabledPrefs;

    @BeforeEach
    void setUp() {
        handler = new OrderDeliveredHandler(notificationService, preferencesService, emailService);

        userId = UUID.randomUUID();
        event = new OrderDeliveredEvent(
                this,
                "ORD-12345",
                userId,
                "customer@example.com",
                "John"
        );

        enabledPrefs = NotificationPreferencesResponse.builder()
                .userId(userId)
                .shippingNotifications(true)
                .build();

        disabledPrefs = NotificationPreferencesResponse.builder()
                .userId(userId)
                .shippingNotifications(false)
                .build();
    }

    @Test
    @DisplayName("Should return correct event type")
    void shouldReturnCorrectEventType() {
        assertThat(handler.getEventType()).isEqualTo(OrderDeliveredEvent.class);
    }

    @Nested
    @DisplayName("When shipping notifications enabled")
    class WhenShippingNotificationsEnabled {

        @BeforeEach
        void setUp() {
            when(preferencesService.getPreferences(userId)).thenReturn(enabledPrefs);
            when(emailService.buildOrderDeliveredEmail(anyString(), anyString()))
                    .thenReturn("<html>Order delivered email</html>");
        }

        @Test
        @DisplayName("Should create in-app notification")
        void shouldCreateInAppNotification() {
            handler.handle(event);

            verify(notificationService).createAndSend(
                    eq(userId),
                    eq(NotificationType.ORDER_DELIVERED),
                    contains("Delivered"),
                    contains("ORD-12345"),
                    eq("/orders"),
                    eq("ORD-12345")
            );
        }

        @Test
        @DisplayName("Should send email notification")
        void shouldSendEmailNotification() {
            handler.handle(event);

            verify(emailService).buildOrderDeliveredEmail("John", "ORD-12345");
            verify(emailService).sendEmail(
                    eq("customer@example.com"),
                    contains("ORD-12345"),
                    anyString()
            );
        }
    }

    @Nested
    @DisplayName("When shipping notifications disabled")
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
        @DisplayName("Should not send email notification")
        void shouldNotSendEmailNotification() {
            handler.handle(event);

            verify(emailService, never()).sendEmail(any(), any(), any());
        }
    }
}
