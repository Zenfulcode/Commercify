package com.zenfulcode.commercify.commercify.api.responses;

public record CancelPaymentResponse(boolean success, String message) {

    public static CancelPaymentResponse PaymentNotFound() {
        return new CancelPaymentResponse(false, "Payment not found");
    }

    public static CancelPaymentResponse PaymentAlreadyPaid() {
        return new CancelPaymentResponse(false, "Payment already paid");
    }

    public static CancelPaymentResponse PaymentAlreadyCanceled() {
        return new CancelPaymentResponse(false, "Payment already canceled");
    }

    public static CancelPaymentResponse InvalidPaymentProvider() {
        return new CancelPaymentResponse(false, "Invalid payment provider");
    }
}
