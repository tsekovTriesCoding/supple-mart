package app.payment.service;

import app.exception.BadRequestException;
import app.order.dto.OrderDTO;
import app.order.model.OrderStatus;
import app.order.service.OrderService;
import app.payment.config.StripeConfig;
import app.payment.dto.PaymentIntentRequest;
import app.payment.dto.PaymentIntentResponse;
import app.user.model.User;
import app.user.service.UserService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final UserService userService;
    private final OrderService orderService;
    private final StripeConfig stripeConfig;

    @Transactional
    public PaymentIntentResponse createPaymentIntent(UUID userId, PaymentIntentRequest request) {
        try {
            User user = userService.getUserById(userId);

            OrderDTO order = orderService.getOrderById(request.getOrderId(), userId);

            if (!"pending".equals(order.getStatus())) {
                throw new BadRequestException("Payment intent can only be created for pending orders");
            }

            // Stripe expects amount in cents (smallest currency unit)
            long amountInCents = order.getTotalAmount().multiply(BigDecimal.valueOf(100)).longValue();

            Map<String, String> metadata = new HashMap<>();
            metadata.put("userId", userId.toString());
            metadata.put("userEmail", user.getEmail());
            metadata.put("orderId", order.getId().toString());
            metadata.put("shippingAddress", order.getShippingAddress());

            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInCents)
                    .setCurrency(request.getCurrency())
                    .putAllMetadata(metadata)
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .build()
                    )
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);

            // Link payment intent to order
            orderService.updateOrderPaymentIntentId(order.getId(), paymentIntent.getId());

            log.info("Payment intent created successfully: {} for user: {} and order: {}",
                    paymentIntent.getId(), userId, order.getId());

            return PaymentIntentResponse.builder()
                    .clientSecret(paymentIntent.getClientSecret())
                    .paymentIntentId(paymentIntent.getId())
                    .amount(paymentIntent.getAmount())
                    .currency(paymentIntent.getCurrency())
                    .status(paymentIntent.getStatus())
                    .build();

        } catch (StripeException e) {
            log.error("Stripe error while creating payment intent: {}", e.getMessage(), e);
            throw new BadRequestException("Failed to create payment intent: " + e.getMessage());
        }
    }

    public Event constructWebhookEvent(String payload, String signatureHeader) {
        try {
            return Webhook.constructEvent(payload, signatureHeader, stripeConfig.getWebhookSecret());
        } catch (SignatureVerificationException e) {
            log.error("Invalid Stripe webhook signature: {}", e.getMessage());
            throw new BadRequestException("Invalid webhook signature");
        }
    }

    /**
     * Handles webhook events from Stripe
     * Maps Stripe payment statuses to OrderStatus:
     * - payment_intent.succeeded -> PAID
     * - payment_intent.processing -> PROCESSING
     * - payment_intent.canceled -> CANCELLED
     */
    @Transactional
    public void handleWebhookEvent(Event event) {
        log.info("Processing Stripe webhook event: {}", event.getType());

        switch (event.getType()) {
            case "payment_intent.succeeded" -> handlePaymentIntentSucceeded(event);
            case "payment_intent.processing" -> handlePaymentIntentProcessing(event);
            case "payment_intent.canceled" -> handlePaymentIntentCanceled(event);
            default -> log.debug("Unhandled event type: {}", event.getType());
        }
    }

    /**
     * Handles successful payment - updates order status to PAID
     * Order flow: PENDING -> PAID (payment completed)
     */
    private void handlePaymentIntentSucceeded(Event event) {
        PaymentIntent paymentIntent = deserializePaymentIntent(event);
        String paymentIntentId = paymentIntent.getId();

        log.info("Payment succeeded for payment intent: {}", paymentIntentId);

        orderService.updateOrderStatusByPaymentIntentId(paymentIntentId, OrderStatus.PAID);
        log.info("Order status updated to PAID for payment intent: {}", paymentIntentId);
    }

    /**
     * Handles processing payment - updates order status to PROCESSING
     * Order flow: PENDING -> PROCESSING (payment being verified)
     * This is typically used for payments that require additional verification (e.g., bank transfers)
     */
    private void handlePaymentIntentProcessing(Event event) {
        PaymentIntent paymentIntent = deserializePaymentIntent(event);
        String paymentIntentId = paymentIntent.getId();

        log.info("Payment processing for payment intent: {}", paymentIntentId);

        orderService.updateOrderStatusByPaymentIntentId(paymentIntentId, OrderStatus.PROCESSING);
        log.info("Order status updated to PROCESSING for payment intent: {}", paymentIntentId);
    }

    /**
     * Handles canceled payment - updates order status to CANCELLED
     * Order flow: PENDING/PROCESSING -> CANCELLED (payment canceled by user or system)
     */
    private void handlePaymentIntentCanceled(Event event) {
        PaymentIntent paymentIntent = deserializePaymentIntent(event);
        String paymentIntentId = paymentIntent.getId();

        log.info("Payment canceled for payment intent: {}", paymentIntentId);

        orderService.updateOrderStatusByPaymentIntentId(paymentIntentId, OrderStatus.CANCELLED);
        log.info("Order status updated to CANCELLED for payment intent: {}", paymentIntentId);
    }

    /**
     * Extracts PaymentIntent from webhook event
     * Uses modern Stripe SDK deserialization with proper error handling
     */
    private PaymentIntent deserializePaymentIntent(Event event) {
        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();

        if (deserializer.getObject().isPresent()) {
            StripeObject stripeObject = deserializer.getObject().get();
            if (stripeObject instanceof PaymentIntent paymentIntent) {
                log.debug("Successfully deserialized PaymentIntent: {}", paymentIntent.getId());
                return paymentIntent;
            }
        }

        log.error("Failed to deserialize PaymentIntent from event: {}. Event type: {}",
                event.getId(), event.getType());
        throw new IllegalStateException("Unable to deserialize PaymentIntent from webhook event");
    }
}
