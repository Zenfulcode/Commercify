package com.zenfulcode.commercify.commercify.controller;

import com.zenfulcode.commercify.commercify.PaymentProvider;
import com.zenfulcode.commercify.commercify.PaymentStatus;
import com.zenfulcode.commercify.commercify.api.requests.PaymentRequest;
import com.zenfulcode.commercify.commercify.api.responses.CancelPaymentResponse;
import com.zenfulcode.commercify.commercify.api.responses.PaymentResponse;
import com.zenfulcode.commercify.commercify.service.PaymentService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/api/v1/payments")
@AllArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @GetMapping("/{orderId}/status")
    public ResponseEntity<String> getPaymentStatus(@PathVariable Long orderId) {
        PaymentStatus status = paymentService.getPaymentStatus(orderId);
        return ResponseEntity.ok(status.name());
    }

    @GetMapping("/cancel/{orderId}")
    public ResponseEntity<CancelPaymentResponse> cancelPayment(@PathVariable Long orderId) {
        CancelPaymentResponse response = paymentService.cancelPayment(orderId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/pay/{paymentProvider}")
    public ResponseEntity<PaymentResponse> makePayment(@PathVariable String paymentProvider, @RequestBody PaymentRequest paymentRequest) {
        PaymentProvider provider = PaymentProvider.valueOf(paymentProvider.toUpperCase());
        PaymentResponse response = paymentService.makePayment(provider, paymentRequest);
        return ResponseEntity.ok(response);
    }
}