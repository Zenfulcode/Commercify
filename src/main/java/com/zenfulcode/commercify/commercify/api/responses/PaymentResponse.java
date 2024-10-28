package com.zenfulcode.commercify.commercify.api.responses;


import com.zenfulcode.commercify.commercify.PaymentStatus;

public record PaymentResponse(Long paymentId, PaymentStatus status, String redirectUrl) {
    public static PaymentResponse FailedPayment() {
        return new PaymentResponse(-1L, PaymentStatus.FAILED, "");
    }
}

