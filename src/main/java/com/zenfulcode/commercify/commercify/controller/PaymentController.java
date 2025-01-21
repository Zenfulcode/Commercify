package com.zenfulcode.commercify.commercify.controller;

import com.zenfulcode.commercify.commercify.PaymentStatus;
import com.zenfulcode.commercify.commercify.api.requests.CapturePaymentRequest;
import com.zenfulcode.commercify.commercify.integration.mobilepay.MobilePayService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/api/v1/payments")
@AllArgsConstructor
@Slf4j
public class PaymentController {
    private final MobilePayService mobilePayService;

    @PostMapping("/{orderId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> updatePaymentStatus(
            @PathVariable Long orderId,
            @RequestParam PaymentStatus status) {
        try {
            mobilePayService.handlePaymentStatusUpdate(orderId, status);
            return ResponseEntity.ok("Payment status updated successfully");
        } catch (Exception e) {
            log.error("Error updating payment status", e);
            return ResponseEntity.badRequest().body("Error updating payment status");
        }
    }

    @GetMapping("/{orderId}/status")
    public ResponseEntity<PaymentStatus> getPaymentStatus(@PathVariable Long orderId) {
        PaymentStatus status = mobilePayService.getPaymentStatus(orderId);
        return ResponseEntity.ok(status);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{orderId}/capture")
    public ResponseEntity<String> capturePayment(@PathVariable Long orderId, @RequestBody CapturePaymentRequest request) {
        try {
            mobilePayService.capturePayment(orderId, request.captureAmount(), request.isPartialCapture());
            return ResponseEntity.ok("Payment captured successfully");
        } catch (Exception e) {
            log.error("Error capturing payment", e);
            return ResponseEntity.badRequest().body("Error capturing payment");
        }
    }
}