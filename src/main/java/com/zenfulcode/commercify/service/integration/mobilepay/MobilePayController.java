package com.zenfulcode.commercify.service.integration.mobilepay;

import com.zenfulcode.commercify.web.dto.request.payment.PaymentRequest;
import com.zenfulcode.commercify.web.dto.response.payment.PaymentResponse;
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
    public ResponseEntity<String> handleCallback(
            @RequestParam String paymentReference,
            @RequestParam String status) {
        try {
            mobilePayService.handlePaymentCallback(paymentReference, status);
            return ResponseEntity.ok("Callback processed successfully");
        } catch (Exception e) {
            log.error("Error processing MobilePay callback", e);
            return ResponseEntity.badRequest().body("Error processing callback");
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestParam String paymentReference,
            @RequestParam String status) {
        try {
            mobilePayService.handlePaymentCallback(paymentReference, status);
            return ResponseEntity.ok("Callback processed successfully");
        } catch (Exception e) {
            log.error("Error processing MobilePay callback", e);
            return ResponseEntity.badRequest().body("Error processing callback");
        }
    }
}