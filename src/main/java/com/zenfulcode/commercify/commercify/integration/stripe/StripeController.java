package com.zenfulcode.commercify.commercify.integration.stripe;

import com.zenfulcode.commercify.commercify.api.requests.PaymentRequest;
import com.zenfulcode.commercify.commercify.api.responses.PaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments/stripe")
@RequiredArgsConstructor
public class StripeController {
    private final StripeService stripeService;
    private final StripeWebhookHandler webhookHandler;

    @PostMapping("/create")
    public ResponseEntity<PaymentResponse> createPayment(@RequestBody PaymentRequest request) {
        try {
            PaymentResponse response = stripeService.initiatePayment(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(PaymentResponse.FailedPayment());
        }
    }


    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        webhookHandler.handleWebhookEvent(payload, sigHeader);
        return ResponseEntity.ok().build();
    }
}
