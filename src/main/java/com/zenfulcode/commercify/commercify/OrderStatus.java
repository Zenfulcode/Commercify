package com.zenfulcode.commercify.commercify;

public enum OrderStatus {
    PENDING, // Order has been created but not yet confirmed
    CONFIRMED, // Order has been confirmed by the customer
    SHIPPED, // Order has been shipped
    COMPLETED, // Order has been delivered
    CANCELLED, // Order has been cancelled
    FAILED, // Order has failed
    REFUNDED, // Order has been refunded
    RETURNED // Order has been returned
}
