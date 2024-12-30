package com.zenfulcode.commercify.commercify.exception;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(Integer orderId) {
        super("Order not found with ID: " + orderId);
    }
}