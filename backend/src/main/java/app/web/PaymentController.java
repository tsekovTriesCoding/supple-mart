package app.web;

import app.payment.dto.PaymentIntentRequest;
import app.payment.dto.PaymentIntentResponse;
import app.payment.service.PaymentService;
import app.security.CustomUserDetails;
import com.stripe.model.Event;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Payments", description = "Payment processing endpoints (Stripe integration)")
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "Create payment intent", description = "Create a Stripe payment intent for an order")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment intent created successfully",
                    content = @Content(schema = @Schema(implementation = PaymentIntentResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request or order not eligible for payment"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @PostMapping("/create-intent")
    public ResponseEntity<PaymentIntentResponse> createPaymentIntent(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody PaymentIntentRequest request) {

        UUID userId = userDetails.getId();
        log.info("Creating payment intent for user: {}", userId);

        PaymentIntentResponse response = paymentService.createPaymentIntent(userId, request);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Stripe webhook", description = "Handle Stripe webhook events (called by Stripe, not for direct use)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Webhook processed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid webhook signature")
    })
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

