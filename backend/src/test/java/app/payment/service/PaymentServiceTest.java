package app.payment.service;

import app.exception.BadRequestException;
import app.exception.ResourceNotFoundException;
import app.order.dto.OrderResponse;
import app.order.model.OrderStatus;
import app.order.service.OrderService;
import app.payment.config.StripeConfig;
import app.payment.dto.PaymentIntentRequest;
import app.payment.dto.PaymentIntentResponse;
import app.user.model.User;
import app.user.service.UserService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private OrderService orderService;

    @Mock
    private StripeConfig stripeConfig;

    private PaymentService paymentService;

    private final UUID userId = UUID.randomUUID();
    private final UUID orderId = UUID.randomUUID();
    private User testUser;
    private OrderResponse testOrder;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(userService, orderService, stripeConfig);

        testUser = new User();
        testUser.setId(userId);
        testUser.setEmail("test@example.com");

        testOrder = OrderResponse.builder()
                .id(orderId)
                .status("pending")
                .totalAmount(BigDecimal.valueOf(99.99))
                .shippingAddress("123 Test St")
                .build();
    }

    @Nested
    @DisplayName("Create Payment Intent Tests")
    class CreatePaymentIntentTests {

        @Test
        @DisplayName("Should throw BadRequestException when order is not pending")
        void shouldThrowExceptionWhenOrderNotPending() {
            PaymentIntentRequest request = new PaymentIntentRequest();
            request.setOrderId(orderId);
            request.setCurrency("usd");

            OrderResponse completedOrder = OrderResponse.builder()
                    .id(orderId)
                    .status("paid")
                    .totalAmount(BigDecimal.valueOf(99.99))
                    .build();

            when(userService.getUserById(userId)).thenReturn(testUser);
            when(orderService.getOrderById(orderId, userId)).thenReturn(completedOrder);

            assertThatThrownBy(() -> paymentService.createPaymentIntent(userId, request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("Payment intent can only be created for pending orders");
        }
    }

    @Nested
    @DisplayName("Webhook Event Handling Tests")
    class WebhookEventHandlingTests {

        @Mock
        private Event event;

        @Mock
        private EventDataObjectDeserializer deserializer;

        @Mock
        private PaymentIntent paymentIntent;

        @Test
        @DisplayName("Should handle payment_intent.succeeded event")
        void shouldHandlePaymentIntentSucceeded() {
            String paymentIntentId = "pi_test_123";

            when(event.getType()).thenReturn("payment_intent.succeeded");
            when(event.getDataObjectDeserializer()).thenReturn(deserializer);
            when(deserializer.getObject()).thenReturn(Optional.of(paymentIntent));
            when(paymentIntent.getId()).thenReturn(paymentIntentId);

            paymentService.handleWebhookEvent(event);

            verify(orderService).updateOrderStatusByPaymentIntentId(paymentIntentId, OrderStatus.PAID);
        }

        @Test
        @DisplayName("Should handle payment_intent.processing event")
        void shouldHandlePaymentIntentProcessing() {
            String paymentIntentId = "pi_test_123";

            when(event.getType()).thenReturn("payment_intent.processing");
            when(event.getDataObjectDeserializer()).thenReturn(deserializer);
            when(deserializer.getObject()).thenReturn(Optional.of(paymentIntent));
            when(paymentIntent.getId()).thenReturn(paymentIntentId);

            paymentService.handleWebhookEvent(event);

            verify(orderService).updateOrderStatusByPaymentIntentId(paymentIntentId, OrderStatus.PROCESSING);
        }

        @Test
        @DisplayName("Should handle payment_intent.canceled event")
        void shouldHandlePaymentIntentCanceled() {
            String paymentIntentId = "pi_test_123";

            when(event.getType()).thenReturn("payment_intent.canceled");
            when(event.getDataObjectDeserializer()).thenReturn(deserializer);
            when(deserializer.getObject()).thenReturn(Optional.of(paymentIntent));
            when(paymentIntent.getId()).thenReturn(paymentIntentId);

            paymentService.handleWebhookEvent(event);

            verify(orderService).updateOrderStatusByPaymentIntentId(paymentIntentId, OrderStatus.CANCELLED);
        }

        @Test
        @DisplayName("Should handle unhandled event type gracefully")
        void shouldHandleUnhandledEventType() {
            when(event.getType()).thenReturn("charge.refunded");

            paymentService.handleWebhookEvent(event);

            verifyNoInteractions(orderService);
        }

        @Test
        @DisplayName("Should gracefully handle ResourceNotFoundException for succeeded event")
        void shouldHandleResourceNotFoundForSucceeded() {
            String paymentIntentId = "pi_test_123";

            when(event.getType()).thenReturn("payment_intent.succeeded");
            when(event.getDataObjectDeserializer()).thenReturn(deserializer);
            when(deserializer.getObject()).thenReturn(Optional.of(paymentIntent));
            when(paymentIntent.getId()).thenReturn(paymentIntentId);
            doThrow(new ResourceNotFoundException("Order not found with paymentIntentId: " + paymentIntentId))
                    .when(orderService).updateOrderStatusByPaymentIntentId(paymentIntentId, OrderStatus.PAID);

            paymentService.handleWebhookEvent(event);

            verify(orderService).updateOrderStatusByPaymentIntentId(paymentIntentId, OrderStatus.PAID);
        }

        @Test
        @DisplayName("Should gracefully handle ResourceNotFoundException for processing event")
        void shouldHandleResourceNotFoundForProcessing() {
            String paymentIntentId = "pi_test_123";

            when(event.getType()).thenReturn("payment_intent.processing");
            when(event.getDataObjectDeserializer()).thenReturn(deserializer);
            when(deserializer.getObject()).thenReturn(Optional.of(paymentIntent));
            when(paymentIntent.getId()).thenReturn(paymentIntentId);
            doThrow(new ResourceNotFoundException("Order not found with paymentIntentId: " + paymentIntentId))
                    .when(orderService).updateOrderStatusByPaymentIntentId(paymentIntentId, OrderStatus.PROCESSING);

            paymentService.handleWebhookEvent(event);

            verify(orderService).updateOrderStatusByPaymentIntentId(paymentIntentId, OrderStatus.PROCESSING);
        }

        @Test
        @DisplayName("Should gracefully handle ResourceNotFoundException for canceled event")
        void shouldHandleResourceNotFoundForCanceled() {
            String paymentIntentId = "pi_test_123";

            when(event.getType()).thenReturn("payment_intent.canceled");
            when(event.getDataObjectDeserializer()).thenReturn(deserializer);
            when(deserializer.getObject()).thenReturn(Optional.of(paymentIntent));
            when(paymentIntent.getId()).thenReturn(paymentIntentId);
            doThrow(new ResourceNotFoundException("Order not found with paymentIntentId: " + paymentIntentId))
                    .when(orderService).updateOrderStatusByPaymentIntentId(paymentIntentId, OrderStatus.CANCELLED);

            paymentService.handleWebhookEvent(event);

            verify(orderService).updateOrderStatusByPaymentIntentId(paymentIntentId, OrderStatus.CANCELLED);
        }

        @Test
        @DisplayName("Should throw exception when PaymentIntent deserialization fails")
        void shouldThrowExceptionWhenDeserializationFails() {
            when(event.getType()).thenReturn("payment_intent.succeeded");
            when(event.getDataObjectDeserializer()).thenReturn(deserializer);
            when(deserializer.getObject()).thenReturn(Optional.empty());
            when(event.getId()).thenReturn("evt_test_123");

            assertThatThrownBy(() -> paymentService.handleWebhookEvent(event))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Unable to deserialize PaymentIntent from webhook event");
        }
    }
}
