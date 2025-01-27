package com.zenfulcode.commercify.api.payment;

import com.zenfulcode.commercify.payment.application.service.MobilepayWebhookService;
import com.zenfulcode.commercify.payment.domain.model.PaymentProvider;
import com.zenfulcode.commercify.payment.domain.valueobject.WebhookRequest;
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
            PaymentProvider paymentProvider = PaymentProvider.valueOf(provider.toUpperCase());

            WebhookRequest webhookRequest = WebhookRequest.builder()
                    .body(body)
                    .headers(extractHeaders(request))
                    .build();

            webhookService.handleWebhook(paymentProvider, webhookRequest);

            return ResponseEntity.ok(ApiResponse.success("Webhook processed successfully"));
        } catch (Exception e) {
            log.error("Error processing {} webhook: {}", provider, e.getMessage());
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Error processing webhook", "WEBHOOK_ERROR", 400)
            );
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{provider}/register")
    public ResponseEntity<ApiResponse<String>> registerWebhook(
            @PathVariable String provider,
            @RequestBody String callbackUrl
    ) {
        try {
            PaymentProvider paymentProvider = PaymentProvider.valueOf(provider.toUpperCase());

            webhookService.registerWebhook(paymentProvider, callbackUrl);
            return ResponseEntity.ok(ApiResponse.success("Webhook registered successfully"));
        } catch (Exception e) {
            log.error("Error registering {} webhook: {}", provider, e.getMessage());
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Error registering webhook", "WEBHOOK_ERROR", 400)
            );
        }
    }

    private Map<String, String> extractHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        request.getHeaderNames().asIterator().forEachRemaining(name -> headers.put(name, request.getHeader(name)));
        return headers;
    }
}
