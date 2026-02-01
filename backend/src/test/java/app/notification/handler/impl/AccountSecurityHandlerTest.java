package app.notification.handler.impl;

import app.notification.dto.NotificationPreferencesResponse;
import app.notification.event.AccountSecurityEvent;
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
class AccountSecurityHandlerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationPreferencesService preferencesService;

    @Mock
    private EmailService emailService;

    private AccountSecurityHandler handler;

    private UUID userId;
    private AccountSecurityEvent event;
    private NotificationPreferencesResponse enabledPrefs;
    private NotificationPreferencesResponse disabledPrefs;

    @BeforeEach
    void setUp() {
        handler = new AccountSecurityHandler(notificationService, preferencesService, emailService);

        userId = UUID.randomUUID();
        event = new AccountSecurityEvent(
                this,
                userId,
                "user@example.com",
                "John",
                "LOGIN_FROM_NEW_DEVICE",
                "Chrome on Windows 11"
        );

        enabledPrefs = NotificationPreferencesResponse.builder()
                .userId(userId)
                .accountSecurityAlerts(true)
                .build();

        disabledPrefs = NotificationPreferencesResponse.builder()
                .userId(userId)
                .accountSecurityAlerts(false)
                .build();
    }

    @Test
    @DisplayName("Should return correct event type")
    void shouldReturnCorrectEventType() {
        assertThat(handler.getEventType()).isEqualTo(AccountSecurityEvent.class);
    }

    @Nested
    @DisplayName("In-app notification behavior")
    class InAppNotificationTests {

        @Test
        @DisplayName("Should always create in-app notification regardless of preferences")
        void shouldAlwaysCreateInAppNotification() {
            when(preferencesService.getPreferences(userId)).thenReturn(disabledPrefs);

            handler.handle(event);

            verify(notificationService).createAndSend(
                    eq(userId),
                    eq(NotificationType.ACCOUNT_SECURITY),
                    contains("Security Alert"),
                    contains("LOGIN_FROM_NEW_DEVICE"),
                    eq("/account"),
                    isNull()
            );
        }
    }

    @Nested
    @DisplayName("Email notification behavior")
    class EmailNotificationTests {

        @Test
        @DisplayName("Should send email when security alerts enabled")
        void shouldSendEmailWhenEnabled() {
            when(preferencesService.getPreferences(userId)).thenReturn(enabledPrefs);
            when(emailService.buildSecurityAlertEmail(anyString(), anyString(), anyString()))
                    .thenReturn("<html>Security alert email</html>");

            handler.handle(event);

            verify(emailService).buildSecurityAlertEmail("John", "LOGIN_FROM_NEW_DEVICE", "Chrome on Windows 11");
            verify(emailService).sendEmail(
                    eq("user@example.com"),
                    contains("Security Alert"),
                    anyString()
            );
        }

        @Test
        @DisplayName("Should not send email when security alerts disabled")
        void shouldNotSendEmailWhenDisabled() {
            when(preferencesService.getPreferences(userId)).thenReturn(disabledPrefs);

            handler.handle(event);

            verify(emailService, never()).sendEmail(any(), any(), any());
        }
    }
}
