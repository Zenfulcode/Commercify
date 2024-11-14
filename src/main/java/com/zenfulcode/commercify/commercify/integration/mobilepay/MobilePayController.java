package com.zenfulcode.commercify.commercify.integration.mobilepay;

import com.zenfulcode.commercify.commercify.api.requests.PaymentRequest;
import com.zenfulcode.commercify.commercify.api.responses.PaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments/mobilepay")
@RequiredArgsConstructor
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
}