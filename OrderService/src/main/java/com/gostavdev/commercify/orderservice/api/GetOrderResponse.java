package com.gostavdev.commercify.orderservice.api;

import com.gostavdev.commercify.orderservice.dto.OrderDetails;

public record GetOrderResponse(OrderDetails orderDetails, String message) {
    public static GetOrderResponse from(OrderDetails orderDetails) {
        return new GetOrderResponse(orderDetails, null);
    }

    public static GetOrderResponse from(String message) {
        return new GetOrderResponse(null, message);
    }
}
