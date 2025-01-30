package com.zenfulcode.commercify.order.domain.model;

public enum OrderStatus {
    PENDING,      // Order has been created but not yet confirmed
    CONFIRMED,    // Order has been confirmed by the customer
    SHIPPED,      // Order has been shipped
    PAID,         // Order has been paid
    COMPLETED,    // Order has been delivered
    CANCELLED,    // Order has been cancelled
    FAILED,       // Order has failed
    REFUNDED,     // Order has been refunded
    RETURNED      // Order has been returned
}
