package com.zenfulcode.commercify.payment.domain.valueobject;

public enum PaymentStatus {
    PENDING,            // Payment has been initiated but not completed
    RESERVED,           // Payment has been reserved but not captured
    CAPTURED,           // Payment has been successfully captured
    FAILED,             // Payment attempt failed
    CANCELLED,          // Payment was cancelled
    REFUNDED,           // Payment was fully refunded
    PARTIALLY_REFUNDED, // Payment was partially refunded
    EXPIRED,            // Payment expired before completion
    TERMINATED          // Payment was terminated by the system
}

