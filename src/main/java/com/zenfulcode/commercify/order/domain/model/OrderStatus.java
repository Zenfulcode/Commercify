package com.zenfulcode.commercify.order.domain.model;

public enum OrderStatus {
    PENDING,    // Order has been created
    ABANDONED,  // Order has been abandoned
    PAID,       // Order has been paid
    SHIPPED,    // Order has been shipped
    COMPLETED,  // Order has been delivered
    CANCELLED,  // Order has been cancelled
    REFUNDED    // Order has been refunded/returned
}
