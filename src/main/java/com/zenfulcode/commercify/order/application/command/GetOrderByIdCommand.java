package com.zenfulcode.commercify.order.application.command;

public record GetOrderByIdCommand(String orderId) {
    public GetOrderByIdCommand {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("Order ID cannot be null or empty");
        }
    }
}
