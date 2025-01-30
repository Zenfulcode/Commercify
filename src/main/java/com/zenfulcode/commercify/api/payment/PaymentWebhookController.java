package com.zenfulcode.commercify.api.payment;

import com.zenfulcode.commercify.api.payment.request.MobilepayWebhookRegistrationRequest;
import com.zenfulcode.commercify.payment.application.service.MobilepayWebhookService;
import com.zenfulcode.commercify.payment.application.service.PaymentApplicationService;
import com.zenfulcode.commercify.payment.domain.model.PaymentProvider;
import com.zenfulcode.commercify.payment.domain.valueobject.WebhookRequest;
import com.zenfulcode.commercify.payment.domain.valueobject.webhook.WebhookPayload;
import com.zenfulcode.commercify.shared.interfaces.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v2/payments/webhooks")
@RequiredArgsConstructor
@Slf4j
public class PaymentWebhookController {
    private final PaymentApplicationService paymentService;
    private final MobilepayWebhookService webhookService;

    @PostMapping("/{provider}/callback")
    public ResponseEntity<ApiResponse<String>> handleCallback(
            @PathVariable String provider,
            @RequestBody String body,
            HttpServletRequest request
    ) {
        PaymentProvider paymentProvider = paymentService.getPaymentProvider(provider);

        WebhookRequest webhookRequest = WebhookRequest.builder()
                .body(body)
                .headers(extractHeaders(request))
                .build();

        WebhookPayload payload = webhookService.authenticate(paymentProvider, webhookRequest);
        paymentService.handlePaymentCallback(paymentProvider, payload);

        return ResponseEntity.ok(ApiResponse.success("Webhook processed successfully"));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{provider}")
    public ResponseEntity<ApiResponse<String>> registerWebhook(
            @PathVariable String provider,
            @RequestBody MobilepayWebhookRegistrationRequest request
    ) {
        PaymentProvider paymentProvider = paymentService.getPaymentProvider(provider);
        webhookService.registerWebhook(paymentProvider, request.callbackUrl());
        return ResponseEntity.ok(ApiResponse.success("Webhook registered successfully"));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{provider}")
    public ResponseEntity<ApiResponse<Object>> getWebhooks(@PathVariable String provider) {
        PaymentProvider paymentProvider = paymentService.getPaymentProvider(provider);
        Object webhooks = webhookService.getWebhooks(paymentProvider);
        return ResponseEntity.ok(ApiResponse.success(webhooks));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{provider}/{webhookId}")
    public ResponseEntity<ApiResponse<Object>> getWebhooks(@PathVariable String provider, @PathVariable String webhookId) {
        PaymentProvider paymentProvider = paymentService.getPaymentProvider(provider);
        webhookService.deleteWebhook(paymentProvider, webhookId);
        return ResponseEntity.ok(ApiResponse.success("Webhook deleted successfully"));
    }

    private Map<String, String> extractHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        request.getHeaderNames().asIterator().forEachRemaining(name -> headers.put(name, request.getHeader(name)));
        return headers;
    }
}
