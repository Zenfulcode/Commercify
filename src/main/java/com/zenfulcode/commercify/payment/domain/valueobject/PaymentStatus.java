package com.zenfulcode.commercify.payment.domain.valueobject;

import lombok.Getter;

@Getter
public enum PaymentStatus {
    PENDING(false),            // Payment has been initiated but not completed
    RESERVED(false),           // Payment has been reserved but not captured
    CAPTURED(false),           // Payment has been successfully captured
    FAILED(false),             // Payment attempt failed
    CANCELLED(true),          // Payment was cancelled
    REFUNDED(true),           // Payment was fully refunded
    PARTIALLY_REFUNDED(false), // Payment was partially refunded
    EXPIRED(true),            // Payment expired before completion
    TERMINATED(true);          // Payment was terminated by the system

    private final boolean terminalState;

    PaymentStatus(boolean terminalState) {
        this.terminalState = terminalState;
    }
}

