package com.zenfulcode.commercify.commercify.controller;

import com.zenfulcode.commercify.commercify.PaymentStatus;
import com.zenfulcode.commercify.commercify.service.PaymentService;
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
    private final PaymentService paymentService;

    @PostMapping("/{orderId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> updatePaymentStatus(
            @PathVariable Integer orderId,
            @RequestParam PaymentStatus status) {
        try {
            paymentService.handlePaymentStatusUpdate(orderId, status);
            return ResponseEntity.ok("Payment status updated successfully");
        } catch (Exception e) {
            log.error("Error updating payment status", e);
            return ResponseEntity.badRequest().body("Error updating payment status");
        }
    }

    @GetMapping("/{orderId}/status")
    public ResponseEntity<PaymentStatus> getPaymentStatus(@PathVariable Integer orderId) {
        PaymentStatus status = paymentService.getPaymentStatus(orderId);
        return ResponseEntity.ok(status);
    }
}