package app.notification.handler;

import app.notification.event.OrderPlacedEvent;
import app.notification.event.OrderShippedEvent;
import app.notification.event.AccountSecurityEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for NotificationEventDispatcher.
 * Tests the Strategy Pattern implementation for routing events to handlers.
 */
@ExtendWith(MockitoExtension.class)
class NotificationEventDispatcherTest {

    @Mock
    private NotificationHandler<OrderPlacedEvent> orderPlacedHandler;

    @Mock
    private NotificationHandler<OrderShippedEvent> orderShippedHandler;

    @Mock
    private NotificationHandler<AccountSecurityEvent> accountSecurityHandler;

    private NotificationEventDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        // Configure mock handlers to return their event types
        when(orderPlacedHandler.getEventType()).thenReturn(OrderPlacedEvent.class);
        when(orderShippedHandler.getEventType()).thenReturn(OrderShippedEvent.class);
        when(accountSecurityHandler.getEventType()).thenReturn(AccountSecurityEvent.class);

        // Create dispatcher with all handlers
        dispatcher = new NotificationEventDispatcher(
                List.of(orderPlacedHandler, orderShippedHandler, accountSecurityHandler)
        );
    }

    @Nested
    @DisplayName("Handler Registration Tests")
    class HandlerRegistrationTests {

        @Test
        @DisplayName("Should register all handlers on construction")
        void shouldRegisterAllHandlers() {
            assertThat(dispatcher.hasHandlerFor(OrderPlacedEvent.class)).isTrue();
            assertThat(dispatcher.hasHandlerFor(OrderShippedEvent.class)).isTrue();
            assertThat(dispatcher.hasHandlerFor(AccountSecurityEvent.class)).isTrue();
        }

        @Test
        @DisplayName("Should return false for unregistered event types")
        void shouldReturnFalseForUnregisteredEventTypes() {
            // Some random class that has no handler
            assertThat(dispatcher.hasHandlerFor(String.class)).isFalse();
        }

        @Test
        @DisplayName("Should initialize with empty handler list")
        void shouldInitializeWithEmptyHandlerList() {
            NotificationEventDispatcher emptyDispatcher = 
                    new NotificationEventDispatcher(Collections.emptyList());
            
            assertThat(emptyDispatcher.hasHandlerFor(OrderPlacedEvent.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("Event Dispatching Tests")
    class EventDispatchingTests {

        @Test
        @DisplayName("Should dispatch OrderPlacedEvent to correct handler")
        void shouldDispatchOrderPlacedEventToCorrectHandler() {
            OrderPlacedEvent event = new OrderPlacedEvent(
                    this,
                    "ORD-123",
                    UUID.randomUUID(),
                    "test@example.com",
                    "John",
                    new BigDecimal("99.99")
            );

            dispatcher.handleEvent(event);

            verify(orderPlacedHandler, times(1)).handle(event);
            verify(orderShippedHandler, never()).handle(any());
            verify(accountSecurityHandler, never()).handle(any());
        }

        @Test
        @DisplayName("Should dispatch OrderShippedEvent to correct handler")
        void shouldDispatchOrderShippedEventToCorrectHandler() {
            OrderShippedEvent event = new OrderShippedEvent(
                    this,
                    "ORD-456",
                    UUID.randomUUID(),
                    "test@example.com",
                    "Jane",
                    "TRACK123"
            );

            dispatcher.handleEvent(event);

            verify(orderShippedHandler, times(1)).handle(event);
            verify(orderPlacedHandler, never()).handle(any());
            verify(accountSecurityHandler, never()).handle(any());
        }

        @Test
        @DisplayName("Should dispatch AccountSecurityEvent to correct handler")
        void shouldDispatchAccountSecurityEventToCorrectHandler() {
            AccountSecurityEvent event = new AccountSecurityEvent(
                    this,
                    UUID.randomUUID(),
                    "test@example.com",
                    "John",
                    "LOGIN_FROM_NEW_DEVICE",
                    "Chrome on Windows"
            );

            dispatcher.handleEvent(event);

            verify(accountSecurityHandler, times(1)).handle(event);
            verify(orderPlacedHandler, never()).handle(any());
            verify(orderShippedHandler, never()).handle(any());
        }

        @Test
        @DisplayName("Should ignore events from other packages")
        void shouldIgnoreEventsFromOtherPackages() {
            // Spring internal events or other non-notification events
            Object springEvent = new Object() {
                @Override
                public String toString() {
                    return "SpringInternalEvent";
                }
            };

            dispatcher.handleEvent(springEvent);

            verify(orderPlacedHandler, never()).handle(any());
            verify(orderShippedHandler, never()).handle(any());
            verify(accountSecurityHandler, never()).handle(any());
        }
    }

    @Nested
    @DisplayName("Multiple Events Tests")
    class MultipleEventsTests {

        @Test
        @DisplayName("Should handle multiple different events correctly")
        void shouldHandleMultipleDifferentEventsCorrectly() {
            UUID userId = UUID.randomUUID();

            OrderPlacedEvent orderEvent = new OrderPlacedEvent(
                    this, "ORD-001", userId, "test@example.com", "John", new BigDecimal("50.00")
            );
            OrderShippedEvent shippedEvent = new OrderShippedEvent(
                    this, "ORD-001", userId, "test@example.com", "John", "TRACK001"
            );

            dispatcher.handleEvent(orderEvent);
            dispatcher.handleEvent(shippedEvent);

            verify(orderPlacedHandler, times(1)).handle(orderEvent);
            verify(orderShippedHandler, times(1)).handle(shippedEvent);
        }

        @Test
        @DisplayName("Should handle same event type multiple times")
        void shouldHandleSameEventTypeMultipleTimes() {
            OrderPlacedEvent event1 = new OrderPlacedEvent(
                    this, "ORD-001", UUID.randomUUID(), "test1@example.com", "John", new BigDecimal("50.00")
            );
            OrderPlacedEvent event2 = new OrderPlacedEvent(
                    this, "ORD-002", UUID.randomUUID(), "test2@example.com", "Jane", new BigDecimal("75.00")
            );

            dispatcher.handleEvent(event1);
            dispatcher.handleEvent(event2);

            verify(orderPlacedHandler, times(1)).handle(event1);
            verify(orderPlacedHandler, times(1)).handle(event2);
            verify(orderPlacedHandler, times(2)).handle(any());
        }
    }
}
