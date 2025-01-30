package com.zenfulcode.commercify.api.payment;

import com.zenfulcode.commercify.api.payment.mapper.PaymentDtoMapper;
import com.zenfulcode.commercify.payment.application.command.CapturePaymentCommand;
import com.zenfulcode.commercify.payment.application.dto.CapturedPayment;
import com.zenfulcode.commercify.payment.application.service.PaymentApplicationService;
import com.zenfulcode.commercify.payment.domain.valueobject.PaymentId;
import com.zenfulcode.commercify.shared.interfaces.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2/payments/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class PaymentAdminController {
    private final PaymentApplicationService paymentService;
    private final PaymentDtoMapper paymentDtoMapper;

    @PostMapping("/{paymentId}/capture")
    public ResponseEntity<ApiResponse<CapturedPayment>> capturePayment(
            @PathVariable String paymentId) {

        CapturePaymentCommand command = paymentDtoMapper.toCaptureCommand(PaymentId.of(paymentId));
        CapturedPayment response = paymentService.capturePayment(command);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{orderId}/refund")
    public ResponseEntity<ApiResponse<String>> refundPayment(
            @PathVariable String orderId) {

//        paymentService.refundPayment(OrderId.of(orderId));
        return ResponseEntity.ok(ApiResponse.success("Refund initiated"));
    }
}
