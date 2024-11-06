package com.zenfulcode.commercify.commercify.api.responses.orders;


import com.zenfulcode.commercify.commercify.viewmodel.OrderViewModel;

public record CreateOrderResponse(
        OrderViewModel order,
        String message) {
    public static CreateOrderResponse from(OrderViewModel order) {
        return new CreateOrderResponse(order, "Order created successfully");
    }

    public static CreateOrderResponse from(String message) {
        return new CreateOrderResponse(null, message);
    }
}
