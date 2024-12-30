package com.zenfulcode.commercify.commercify.api.responses;


import com.zenfulcode.commercify.commercify.PaymentStatus;

public record PaymentResponse(Integer paymentId, PaymentStatus status, String redirectUrl) {
    public static PaymentResponse FailedPayment() {
        return new PaymentResponse(-1, PaymentStatus.FAILED, "");
    }
}

