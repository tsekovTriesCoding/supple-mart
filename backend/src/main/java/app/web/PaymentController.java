package app.web;

import app.payment.dto.PaymentIntentRequest;
import app.payment.dto.PaymentIntentResponse;
import app.payment.service.PaymentService;
import app.security.CustomUserDetails;
import com.stripe.model.Event;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    @PostMapping("/webhook")
    public ResponseEntity<Void> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signatureHeader) {

        log.info("Received Stripe webhook");

        Event event = paymentService.constructWebhookEvent(payload, signatureHeader);
        paymentService.handleWebhookEvent(event);

        log.info("Webhook processed successfully: {}", event.getType());
        return ResponseEntity.ok().build();
    }
}

