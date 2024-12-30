package com.zenfulcode.commercify.web.dto.response.payment;


import com.zenfulcode.commercify.domain.enums.PaymentStatus;

public record PaymentResponse(Long paymentId, PaymentStatus status, String redirectUrl) {
    public static PaymentResponse FailedPayment() {
        return new PaymentResponse(-1L, PaymentStatus.FAILED, "");
    }
}

