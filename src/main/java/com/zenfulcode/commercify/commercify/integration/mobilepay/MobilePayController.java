package com.zenfulcode.commercify.commercify.integration.mobilepay;

import com.zenfulcode.commercify.commercify.api.requests.PaymentRequest;
import com.zenfulcode.commercify.commercify.api.responses.PaymentResponse;
import com.zenfulcode.commercify.commercify.integration.WebhookSubscribeRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments/mobilepay")
@RequiredArgsConstructor
@Slf4j
public class MobilePayController {
    private final MobilePayService mobilePayService;

    @PostMapping("/create")
    public ResponseEntity<PaymentResponse> createPayment(@RequestBody PaymentRequest request) {
        try {
            PaymentResponse response = mobilePayService.initiatePayment(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(PaymentResponse.FailedPayment());
        }
    }

    @PostMapping("/callback")
    public ResponseEntity<String> handleCallback(@RequestHeader("x-ms-date") String date,
                                                 @RequestHeader("x-ms-content-sha256") String contentSha256,
                                                 @RequestHeader("Authorization") String authorization,
                                                 HttpServletRequest request,
                                                 @RequestBody String body) {
        try {
            System.out.println(request.getRequestURI());
            System.out.println(request.getHeader("x-ms-date"));
            System.out.println(request.getHeader("x-ms-content-sha256"));
            System.out.println(request.getHeader("Authorization"));
            System.out.println(body);

            mobilePayService.authenticateRequest(date, contentSha256, authorization, body, request);

//            mobilePayService.handlePaymentCallback(paymentReference, status);
            return ResponseEntity.ok("Callback processed successfully");
        } catch (Exception e) {
            log.error("Error processing MobilePay callback", e);
            return ResponseEntity.badRequest().body("Error processing callback");
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/webhooks")
    public ResponseEntity<?> registerWebhooks(@RequestBody WebhookSubscribeRequest request) {
        try {
            mobilePayService.registerWebhooks(request.callbackUrl());
            return ResponseEntity.ok("Webhooks registered successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error registering webhooks");
        }
    }
}