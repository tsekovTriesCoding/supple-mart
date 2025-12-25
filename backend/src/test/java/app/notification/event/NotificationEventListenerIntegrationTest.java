package app.notification.event;

import app.BaseIntegrationTest;
import app.notification.model.NotificationPreferences;
import app.notification.repository.NotificationPreferencesRepository;
import app.notification.service.EmailService;
import app.user.model.Role;
import app.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Integration tests for notification event listeners.
 * Tests that events are properly published and processed by listeners.
 * Uses @MockitoSpyBean on EmailService to verify email interactions while
 * TestMailConfig mocks the underlying JavaMailSender to prevent actual SMTP calls.
 */
class NotificationEventListenerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private NotificationPreferencesRepository notificationPreferencesRepository;

    @MockitoSpyBean
    private EmailService emailService;

    private User testUser;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setEmail("eventtest-" + UUID.randomUUID() + "@example.com");
        testUser.setFirstName("Event");
        testUser.setLastName("TestUser");
        testUser.setPassword("hashedpassword");
        testUser.setRole(Role.CUSTOMER);
        testUser = userRepository.save(testUser);
        testUserId = testUser.getId();

        NotificationPreferences prefs = new NotificationPreferences();
        prefs.setUserId(testUserId);
        prefs.setOrderUpdates(true);
        prefs.setShippingNotifications(true);
        prefs.setPriceDropAlerts(true);
        prefs.setBackInStockAlerts(true);
        prefs.setReviewReminders(true);
        prefs.setAccountSecurityAlerts(true);
        prefs.setPasswordResetEmails(true);
        prefs.setPromotionalEmails(true);
        prefs.setNewsletter(true);
        prefs.setProductRecommendations(true);
        notificationPreferencesRepository.save(prefs);

        reset(emailService);
    }

    @Nested
    @DisplayName("OrderPlacedEvent Tests")
    class OrderPlacedEventTests {

        @Test
        @DisplayName("Should send order confirmation email when event is published")
        void orderPlacedEvent_SendsConfirmationEmail() {
            String orderNumber = "ORD-TEST-" + System.currentTimeMillis();
            BigDecimal totalAmount = new BigDecimal("99.99");

            OrderPlacedEvent event = new OrderPlacedEvent(
                    this,
                    orderNumber,
                    testUserId,
                    testUser.getEmail(),
                    testUser.getFirstName(),
                    totalAmount
            );

            eventPublisher.publishEvent(event);

            await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
                verify(emailService, times(1)).sendEmail(
                        eq(testUser.getEmail()),
                        contains("Order Confirmation"),
                        anyString()
                );
            });
        }

        @Test
        @DisplayName("Should not send email when order updates are disabled")
        void orderPlacedEvent_NotSentWhenDisabled() {
            NotificationPreferences prefs = notificationPreferencesRepository.findByUserId(testUserId)
                    .orElseThrow();
            prefs.setOrderUpdates(false);
            notificationPreferencesRepository.save(prefs);

            OrderPlacedEvent event = new OrderPlacedEvent(
                    this,
                    "ORD-DISABLED-" + System.currentTimeMillis(),
                    testUserId,
                    testUser.getEmail(),
                    testUser.getFirstName(),
                    new BigDecimal("50.00")
            );

            eventPublisher.publishEvent(event);

            await().pollDelay(1, TimeUnit.SECONDS)
                    .atMost(3, TimeUnit.SECONDS)
                    .untilAsserted(() -> {
                        verify(emailService, never()).sendEmail(
                                eq(testUser.getEmail()),
                                contains("Order Confirmation"),
                                anyString()
                        );
                    });
        }
    }

    @Nested
    @DisplayName("OrderShippedEvent Tests")
    class OrderShippedEventTests {

        @Test
        @DisplayName("Should send shipping notification when order is shipped")
        void orderShippedEvent_SendsShippingNotification() {
            String orderNumber = "ORD-SHIP-" + System.currentTimeMillis();
            String trackingNumber = "TRACK123456";

            OrderShippedEvent event = new OrderShippedEvent(
                    this,
                    orderNumber,
                    testUserId,
                    testUser.getEmail(),
                    testUser.getFirstName(),
                    trackingNumber
            );

            eventPublisher.publishEvent(event);

            await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
                verify(emailService, times(1)).sendEmail(
                        eq(testUser.getEmail()),
                        contains("Shipped"),
                        anyString()
                );
            });
        }

        @Test
        @DisplayName("Should not send email when shipping notifications are disabled")
        void orderShippedEvent_NotSentWhenDisabled() {
            NotificationPreferences prefs = notificationPreferencesRepository.findByUserId(testUserId)
                    .orElseThrow();
            prefs.setShippingNotifications(false);
            notificationPreferencesRepository.save(prefs);

            OrderShippedEvent event = new OrderShippedEvent(
                    this,
                    "ORD-SHIP-DISABLED-" + System.currentTimeMillis(),
                    testUserId,
                    testUser.getEmail(),
                    testUser.getFirstName(),
                    "TRACK999"
            );

            eventPublisher.publishEvent(event);

            await().pollDelay(1, TimeUnit.SECONDS)
                    .atMost(3, TimeUnit.SECONDS)
                    .untilAsserted(() -> {
                        verify(emailService, never()).sendEmail(
                                eq(testUser.getEmail()),
                                contains("Shipped"),
                                anyString()
                        );
                    });
        }
    }

    @Nested
    @DisplayName("OrderDeliveredEvent Tests")
    class OrderDeliveredEventTests {

        @Test
        @DisplayName("Should send delivery notification when order is delivered")
        void orderDeliveredEvent_SendsDeliveryNotification() {
            String orderNumber = "ORD-DELIVER-" + System.currentTimeMillis();

            OrderDeliveredEvent event = new OrderDeliveredEvent(
                    this,
                    orderNumber,
                    testUserId,
                    testUser.getEmail(),
                    testUser.getFirstName()
            );

            eventPublisher.publishEvent(event);

            await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
                verify(emailService, times(1)).sendEmail(
                        eq(testUser.getEmail()),
                        contains("Delivered"),
                        anyString()
                );
            });
        }
    }

    @Nested
    @DisplayName("PriceDropEvent Tests")
    class PriceDropEventTests {

        @Test
        @DisplayName("Should send price drop alerts to interested users")
        void priceDropEvent_SendsAlertsToInterestedUsers() {
            List<PriceDropEvent.UserNotificationData> interestedUsers = List.of(
                    new PriceDropEvent.UserNotificationData(testUserId, testUser.getEmail(), testUser.getFirstName())
            );

            PriceDropEvent event = new PriceDropEvent(
                    this,
                    "Premium Protein Powder",
                    59.99,
                    39.99,
                    interestedUsers
            );

            eventPublisher.publishEvent(event);

            await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
                verify(emailService, times(1)).sendEmail(
                        eq(testUser.getEmail()),
                        contains("Price Drop"),
                        anyString()
                );
            });
        }

        @Test
        @DisplayName("Should not send when price drop alerts are disabled")
        void priceDropEvent_NotSentWhenDisabled() {
            NotificationPreferences prefs = notificationPreferencesRepository.findByUserId(testUserId)
                    .orElseThrow();
            prefs.setPriceDropAlerts(false);
            notificationPreferencesRepository.save(prefs);

            List<PriceDropEvent.UserNotificationData> interestedUsers = List.of(
                    new PriceDropEvent.UserNotificationData(testUserId, testUser.getEmail(), testUser.getFirstName())
            );

            PriceDropEvent event = new PriceDropEvent(
                    this,
                    "Discounted Product",
                    79.99,
                    49.99,
                    interestedUsers
            );

            eventPublisher.publishEvent(event);

            await().pollDelay(1, TimeUnit.SECONDS)
                    .atMost(3, TimeUnit.SECONDS)
                    .untilAsserted(() -> {
                        verify(emailService, never()).sendEmail(
                                eq(testUser.getEmail()),
                                contains("Price Drop"),
                                anyString()
                        );
                    });
        }

        @Test
        @DisplayName("Should send to multiple interested users")
        void priceDropEvent_SendsToMultipleUsers() {
            User secondUser = new User();
            secondUser.setEmail("second-" + UUID.randomUUID() + "@example.com");
            secondUser.setFirstName("Second");
            secondUser.setLastName("User");
            secondUser.setPassword("hashedpassword");
            secondUser.setRole(Role.CUSTOMER);
            secondUser = userRepository.save(secondUser);

            NotificationPreferences secondPrefs = new NotificationPreferences();
            secondPrefs.setUserId(secondUser.getId());
            secondPrefs.setPriceDropAlerts(true);
            notificationPreferencesRepository.save(secondPrefs);

            List<PriceDropEvent.UserNotificationData> interestedUsers = List.of(
                    new PriceDropEvent.UserNotificationData(testUserId, testUser.getEmail(), testUser.getFirstName()),
                    new PriceDropEvent.UserNotificationData(secondUser.getId(), secondUser.getEmail(), secondUser.getFirstName())
            );

            PriceDropEvent event = new PriceDropEvent(
                    this,
                    "Multi-User Product",
                    100.00,
                    75.00,
                    interestedUsers
            );

            eventPublisher.publishEvent(event);

            await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
                verify(emailService, times(2)).sendEmail(
                        anyString(),
                        contains("Price Drop"),
                        anyString()
                );
            });
        }
    }

    @Nested
    @DisplayName("ProductRestockedEvent Tests")
    class ProductRestockedEventTests {

        @Test
        @DisplayName("Should send restock notifications to interested users")
        void productRestockedEvent_SendsNotifications() {
            List<ProductRestockedEvent.UserNotificationData> interestedUsers = List.of(
                    new ProductRestockedEvent.UserNotificationData(testUserId, testUser.getEmail(), testUser.getFirstName())
            );

            ProductRestockedEvent event = new ProductRestockedEvent(
                    this,
                    "Popular Creatine",
                    interestedUsers
            );

            eventPublisher.publishEvent(event);

            await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
                verify(emailService, times(1)).sendEmail(
                        eq(testUser.getEmail()),
                        contains("Back in Stock"),
                        anyString()
                );
            });
        }

        @Test
        @DisplayName("Should not send when back in stock alerts are disabled")
        void productRestockedEvent_NotSentWhenDisabled() {
            NotificationPreferences prefs = notificationPreferencesRepository.findByUserId(testUserId)
                    .orElseThrow();
            prefs.setBackInStockAlerts(false);
            notificationPreferencesRepository.save(prefs);

            List<ProductRestockedEvent.UserNotificationData> interestedUsers = List.of(
                    new ProductRestockedEvent.UserNotificationData(testUserId, testUser.getEmail(), testUser.getFirstName())
            );

            ProductRestockedEvent event = new ProductRestockedEvent(
                    this,
                    "Restocked Item",
                    interestedUsers
            );

            eventPublisher.publishEvent(event);

            await().pollDelay(1, TimeUnit.SECONDS)
                    .atMost(3, TimeUnit.SECONDS)
                    .untilAsserted(() -> {
                        verify(emailService, never()).sendEmail(
                                eq(testUser.getEmail()),
                                contains("Back in Stock"),
                                anyString()
                        );
                    });
        }
    }

    @Nested
    @DisplayName("ReviewReminderEvent Tests")
    class ReviewReminderEventTests {

        @Test
        @DisplayName("Should send review reminder when enabled")
        void reviewReminderEvent_SendsReminder() {
            String orderNumber = "ORD-REVIEW-" + System.currentTimeMillis();

            ReviewReminderEvent event = new ReviewReminderEvent(
                    this,
                    orderNumber,
                    testUserId,
                    testUser.getEmail(),
                    testUser.getFirstName()
            );

            eventPublisher.publishEvent(event);

            await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
                verify(emailService, times(1)).sendEmail(
                        eq(testUser.getEmail()),
                        contains("Feedback"),
                        anyString()
                );
            });
        }

        @Test
        @DisplayName("Should not send when review reminders are disabled")
        void reviewReminderEvent_NotSentWhenDisabled() {
            NotificationPreferences prefs = notificationPreferencesRepository.findByUserId(testUserId)
                    .orElseThrow();
            prefs.setReviewReminders(false);
            notificationPreferencesRepository.save(prefs);

            ReviewReminderEvent event = new ReviewReminderEvent(
                    this,
                    "ORD-NO-REVIEW-" + System.currentTimeMillis(),
                    testUserId,
                    testUser.getEmail(),
                    testUser.getFirstName()
            );

            eventPublisher.publishEvent(event);

            await().pollDelay(1, TimeUnit.SECONDS)
                    .atMost(3, TimeUnit.SECONDS)
                    .untilAsserted(() -> {
                        verify(emailService, never()).sendEmail(
                                eq(testUser.getEmail()),
                                contains("Feedback"),
                                anyString()
                        );
                    });
        }
    }

    @Nested
    @DisplayName("AccountSecurityEvent Tests")
    class AccountSecurityEventTests {

        @Test
        @DisplayName("Should send security alerts when enabled")
        void accountSecurityEvent_SendsAlert() {
            AccountSecurityEvent event = new AccountSecurityEvent(
                    this,
                    testUserId,
                    testUser.getEmail(),
                    testUser.getFirstName(),
                    "New Login Detected",
                    "Login from new device: Windows 10, Chrome Browser"
            );

            eventPublisher.publishEvent(event);

            await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
                verify(emailService, times(1)).sendEmail(
                        eq(testUser.getEmail()),
                        contains("Security Alert"),
                        anyString()
                );
            });
        }

        @Test
        @DisplayName("Should not send when security alerts are disabled")
        void accountSecurityEvent_NotSentWhenDisabled() {
            NotificationPreferences prefs = notificationPreferencesRepository.findByUserId(testUserId)
                    .orElseThrow();
            prefs.setAccountSecurityAlerts(false);
            notificationPreferencesRepository.save(prefs);

            AccountSecurityEvent event = new AccountSecurityEvent(
                    this,
                    testUserId,
                    testUser.getEmail(),
                    testUser.getFirstName(),
                    "Password Changed",
                    "Your password was changed"
            );

            eventPublisher.publishEvent(event);

            await().pollDelay(1, TimeUnit.SECONDS)
                    .atMost(3, TimeUnit.SECONDS)
                    .untilAsserted(() -> {
                        verify(emailService, never()).sendEmail(
                                eq(testUser.getEmail()),
                                contains("Security Alert"),
                                anyString()
                        );
                    });
        }
    }

    @Nested
    @DisplayName("PasswordResetEvent Tests")
    class PasswordResetEventTests {

        @Test
        @DisplayName("Should send password reset email when enabled")
        void passwordResetEvent_SendsEmail() {
            String resetToken = UUID.randomUUID().toString();

            PasswordResetEvent event = new PasswordResetEvent(
                    this,
                    testUserId,
                    testUser.getEmail(),
                    testUser.getFirstName(),
                    resetToken
            );

            eventPublisher.publishEvent(event);

            await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
                verify(emailService, times(1)).sendEmail(
                        eq(testUser.getEmail()),
                        contains("Password Reset"),
                        anyString()
                );
            });
        }

        @Test
        @DisplayName("Should not send when password reset emails are disabled")
        void passwordResetEvent_NotSentWhenDisabled() {
            NotificationPreferences prefs = notificationPreferencesRepository.findByUserId(testUserId)
                    .orElseThrow();
            prefs.setPasswordResetEmails(false);
            notificationPreferencesRepository.save(prefs);

            PasswordResetEvent event = new PasswordResetEvent(
                    this,
                    testUserId,
                    testUser.getEmail(),
                    testUser.getFirstName(),
                    "disabled-token"
            );

            eventPublisher.publishEvent(event);

            await().pollDelay(1, TimeUnit.SECONDS)
                    .atMost(3, TimeUnit.SECONDS)
                    .untilAsserted(() -> {
                        verify(emailService, never()).sendEmail(
                                eq(testUser.getEmail()),
                                contains("Password Reset"),
                                anyString()
                        );
                    });
        }
    }

    @Nested
    @DisplayName("AbandonedCartEvent Tests")
    class AbandonedCartEventTests {

        @Test
        @DisplayName("Should send abandoned cart reminder when promotional emails are enabled")
        void abandonedCartEvent_SendsReminder() {
            List<AbandonedCartEvent.CartItemData> cartItems = List.of(
                    new AbandonedCartEvent.CartItemData("Whey Protein", 2, new BigDecimal("49.99")),
                    new AbandonedCartEvent.CartItemData("BCAA", 1, new BigDecimal("29.99"))
            );

            AbandonedCartEvent event = new AbandonedCartEvent(
                    this,
                    testUserId,
                    testUser.getEmail(),
                    testUser.getFirstName(),
                    cartItems,
                    new BigDecimal("129.97")
            );

            eventPublisher.publishEvent(event);

            await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
                verify(emailService, times(1)).sendEmail(
                        eq(testUser.getEmail()),
                        contains("cart"),
                        anyString()
                );
            });
        }

        @Test
        @DisplayName("Should not send when promotional emails are disabled")
        void abandonedCartEvent_NotSentWhenDisabled() {
            NotificationPreferences prefs = notificationPreferencesRepository.findByUserId(testUserId)
                    .orElseThrow();
            prefs.setPromotionalEmails(false);
            notificationPreferencesRepository.save(prefs);

            List<AbandonedCartEvent.CartItemData> cartItems = List.of(
                    new AbandonedCartEvent.CartItemData("Some Product", 1, new BigDecimal("19.99"))
            );

            AbandonedCartEvent event = new AbandonedCartEvent(
                    this,
                    testUserId,
                    testUser.getEmail(),
                    testUser.getFirstName(),
                    cartItems,
                    new BigDecimal("19.99")
            );

            eventPublisher.publishEvent(event);

            await().pollDelay(1, TimeUnit.SECONDS)
                    .atMost(3, TimeUnit.SECONDS)
                    .untilAsserted(() -> {
                        verify(emailService, never()).sendEmail(
                                eq(testUser.getEmail()),
                                contains("cart"),
                                anyString()
                        );
                    });
        }
    }

    @Nested
    @DisplayName("LowStockAlertEvent Tests (Admin)")
    class LowStockAlertEventTests {

        @Test
        @DisplayName("Should send low stock alert to admin email")
        void lowStockAlertEvent_SendsToAdmin() {
            List<LowStockAlertEvent.ProductStockData> lowStockProducts = List.of(
                    new LowStockAlertEvent.ProductStockData("Creatine Monohydrate", 5),
                    new LowStockAlertEvent.ProductStockData("Pre-Workout", 3)
            );

            List<LowStockAlertEvent.ProductStockData> outOfStockProducts = List.of(
                    new LowStockAlertEvent.ProductStockData("Popular Protein", 0)
            );

            LowStockAlertEvent event = new LowStockAlertEvent(
                    this,
                    lowStockProducts,
                    outOfStockProducts
            );

            eventPublisher.publishEvent(event);

            await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
                verify(emailService, times(1)).sendEmail(
                        anyString(), // admin email
                        contains("Inventory Alert"),
                        anyString()
                );
            });
        }
    }

    @Nested
    @DisplayName("DailyReportEvent Tests (Admin)")
    class DailyReportEventTests {

        @Test
        @DisplayName("Should send daily report to admin")
        void dailyReportEvent_SendsToAdmin() {
            DailyReportEvent event = new DailyReportEvent(
                    this,
                    150, // total orders
                    12,  // pending orders
                    5    // low stock count
            );

            eventPublisher.publishEvent(event);

            await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
                verify(emailService, times(1)).sendEmail(
                        anyString(), // admin email
                        contains("Daily"),
                        anyString()
                );
            });
        }
    }
}
