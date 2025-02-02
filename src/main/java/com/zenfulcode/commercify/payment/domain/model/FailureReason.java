package com.zenfulcode.commercify.payment.domain.model;

import lombok.Getter;

@Getter
public enum FailureReason {
    INSUFFICIENT_FUNDS("Insufficient funds"),
    INVALID_PAYMENT_METHOD("Invalid payment method"),
    PAYMENT_PROVIDER_ERROR("Payment provider error"),
    PAYMENT_METHOD_ERROR("Payment method error"),
    PAYMENT_PROCESSING_ERROR("Payment processing error"),
    PAYMENT_EXPIRED("Payment expired"),
    PAYMENT_TERMINATED("Payment terminated"),
    UNKNOWN("Unknown");

    private final String reason;

    FailureReason(String reason) {
        this.reason = reason;
    }

}
