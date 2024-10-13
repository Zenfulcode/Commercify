package com.gostavdev.commercify.orderservice.api;

import com.gostavdev.commercify.orderservice.dto.OrderDTO;

public record CreateOrderResponse(
        OrderDTO order,
        String message) {
    public static CreateOrderResponse from(OrderDTO order) {
        return new CreateOrderResponse(order, "Order created successfully");
    }

    public static CreateOrderResponse from(String message) {
        return new CreateOrderResponse(null, message);
    }
}
