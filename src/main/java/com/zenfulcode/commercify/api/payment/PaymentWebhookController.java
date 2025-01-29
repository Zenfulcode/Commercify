package com.zenfulcode.commercify.api.payment;

import com.zenfulcode.commercify.api.payment.request.MobilepayWebhookRegistrationRequest;
import com.zenfulcode.commercify.payment.application.service.MobilepayWebhookService;
import com.zenfulcode.commercify.payment.domain.model.PaymentProvider;
import com.zenfulcode.commercify.payment.domain.valueobject.WebhookRequest;
import com.zenfulcode.commercify.shared.domain.exception.DomainException;
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
    private final MobilepayWebhookService webhookService;

    @PostMapping("/{provider}/callback")
    public ResponseEntity<ApiResponse<String>> handleCallback(
            @PathVariable String provider,
            @RequestBody String body,
            HttpServletRequest request
    ) {
        try {
            PaymentProvider paymentProvider = getPaymentProvider(provider);

            WebhookRequest webhookRequest = WebhookRequest.builder()
                    .body(body)
                    .headers(extractHeaders(request))
                    .build();

            webhookService.handleWebhook(paymentProvider, webhookRequest);

            return ResponseEntity.ok(ApiResponse.success("Webhook processed successfully"));
        } catch (DomainException e) {
            log.error("Error processing {} webhook: {}", provider, e.getMessage());
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Error processing webhook", "WEBHOOK_ERROR", 400)
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Invalid payment provider", "INVALID_PROVIDER", 400)
            );
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{provider}")
    public ResponseEntity<ApiResponse<String>> registerWebhook(
            @PathVariable String provider,
            @RequestBody MobilepayWebhookRegistrationRequest request
    ) {
        try {
            PaymentProvider paymentProvider = getPaymentProvider(provider);

            webhookService.registerWebhook(paymentProvider, request.callbackUrl());
            return ResponseEntity.ok(ApiResponse.success("Webhook registered successfully"));
        } catch (DomainException e) {
            log.error("Error registering {} webhook: {}", provider, e.getMessage());
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Error registering webhook", "WEBHOOK_ERROR", 400)
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Invalid payment provider", "INVALID_PROVIDER", 400)
            );
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{provider}")
    public ResponseEntity<ApiResponse<Object>> getWebhooks(@PathVariable String provider) {
        try {
            PaymentProvider paymentProvider = getPaymentProvider(provider);

            Object webhooks = webhookService.getWebhooks(paymentProvider);
            return ResponseEntity.ok(ApiResponse.success(webhooks));
        } catch (DomainException e) {
            log.error("Error getting {} webhooks: {}", provider, e.getMessage());
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Error getting webhooks", "WEBHOOK_ERROR", 400)
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Invalid payment provider", "INVALID_PROVIDER", 400)
            );
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{provider}/{webhookId}")
    public ResponseEntity<ApiResponse<Object>> getWebhooks(@PathVariable String provider, @PathVariable String webhookId) {
        try {
            PaymentProvider paymentProvider = getPaymentProvider(provider);

            webhookService.deleteWebhook(paymentProvider, webhookId);
            return ResponseEntity.ok(ApiResponse.success("Webhook deleted successfully"));
        } catch (DomainException e) {
            log.error("Error deleting {} webhook: {}", provider, e.getMessage());
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Error deleting webhook", "WEBHOOK_ERROR", 400)
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Invalid payment provider", "INVALID_PROVIDER", 400)
            );
        }
    }

    private Map<String, String> extractHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        request.getHeaderNames().asIterator().forEachRemaining(name -> headers.put(name, request.getHeader(name)));
        return headers;
    }

    private PaymentProvider getPaymentProvider(String provider) {
        return PaymentProvider.valueOf(provider.toUpperCase());
    }
}
