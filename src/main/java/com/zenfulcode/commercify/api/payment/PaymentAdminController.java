package com.zenfulcode.commercify.api.payment;

import com.zenfulcode.commercify.api.payment.dto.response.CapturedPaymentResponse;
import com.zenfulcode.commercify.api.payment.mapper.PaymentDtoMapper;
import com.zenfulcode.commercify.order.domain.valueobject.OrderId;
import com.zenfulcode.commercify.payment.application.command.CapturePaymentCommand;
import com.zenfulcode.commercify.payment.application.dto.CapturedPayment;
import com.zenfulcode.commercify.payment.application.service.PaymentApplicationService;
import com.zenfulcode.commercify.payment.domain.valueobject.PaymentId;
import com.zenfulcode.commercify.shared.interfaces.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v2/payments/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class PaymentAdminController {
    private final PaymentApplicationService paymentService;
    private final PaymentDtoMapper paymentDtoMapper;

    @PostMapping("/{orderId}/capture")
    public ResponseEntity<ApiResponse<CapturedPaymentResponse>> capturePayment(
            @PathVariable String orderId) {

        CapturePaymentCommand command = paymentDtoMapper.toCaptureCommand(OrderId.of(orderId));
        CapturedPayment capturedPayment = paymentService.capturePayment(command);
        CapturedPaymentResponse response = paymentDtoMapper.toCapturedResponse(capturedPayment);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{orderId}/refund")
    public ResponseEntity<ApiResponse<String>> refundPayment(
            @PathVariable String orderId) {

//        paymentService.refundPayment(OrderId.of(orderId));
        return ResponseEntity.ok(ApiResponse.success("Refund initiated"));
    }

}
