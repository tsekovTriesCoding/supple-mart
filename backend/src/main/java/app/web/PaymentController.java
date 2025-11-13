package app.web;

import app.payment.dto.PaymentIntentRequest;
import app.payment.dto.PaymentIntentResponse;
import app.payment.service.PaymentService;
import app.security.CustomUserDetails;
import com.stripe.model.Event;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create-intent")
    public ResponseEntity<PaymentIntentResponse> createPaymentIntent(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody PaymentIntentRequest request) {

        UUID userId = userDetails.getId();
        log.info("Creating payment intent for user: {}", userId);

        PaymentIntentResponse response = paymentService.createPaymentIntent(userId, request);

        return ResponseEntity.ok(response);
    }

    /**
     * Stripe webhook endpoint - handles payment events
     * This endpoint is called by Stripe when payment events occur
     */
    @PostMapping(value = "/webhook", consumes = "application/json")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "Stripe-Signature", required = false) String signatureHeader) {

        log.info("Received Stripe webhook, payload length: {}, signature present: {}",
                 payload != null ? payload.length() : 0, signatureHeader != null);

        try {
            if (signatureHeader == null || signatureHeader.isEmpty()) {
                log.warn("Webhook received without Stripe-Signature header");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Missing Stripe-Signature header");
            }

            // Construct and verify the webhook event
            Event event = paymentService.constructWebhookEvent(payload, signatureHeader);

            // Process the webhook event
            paymentService.handleWebhookEvent(event);

            log.info("Webhook processed successfully: {}", event.getType());
            return ResponseEntity.ok("Webhook processed");

        } catch (Exception e) {
            log.error("Error processing webhook: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Webhook processing failed: " + e.getMessage());
        }
    }
}

