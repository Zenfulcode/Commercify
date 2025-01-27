package com.zenfulcode.commercify.api.payment;

import com.zenfulcode.commercify.api.payment.mapper.PaymentDtoMapper;
import com.zenfulcode.commercify.api.payment.request.InitiatePaymentRequest;
import com.zenfulcode.commercify.api.payment.response.PaymentResponse;
import com.zenfulcode.commercify.payment.application.command.InitiatePaymentCommand;
import com.zenfulcode.commercify.payment.application.dto.InitializedPayment;
import com.zenfulcode.commercify.payment.application.service.PaymentApplicationService;
import com.zenfulcode.commercify.shared.interfaces.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentApplicationService paymentService;
    private final PaymentDtoMapper paymentDtoMapper;

    @PostMapping("/initiate")
    public ResponseEntity<ApiResponse<PaymentResponse>> initiatePayment(
            @RequestBody InitiatePaymentRequest request) {

        InitiatePaymentCommand command = paymentDtoMapper.toCommand(request);
        InitializedPayment response = paymentService.initiatePayment(command);

        return ResponseEntity.ok(ApiResponse.success(paymentDtoMapper.toResponse(response)));
    }
}
