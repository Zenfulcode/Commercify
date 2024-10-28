package com.zenfulcode.commercify.commercify.api.responses;


import com.zenfulcode.commercify.commercify.dto.OrderDetailsDTO;

public record GetOrderResponse(OrderDetailsDTO orderDetails, String message) {
    public static GetOrderResponse from(OrderDetailsDTO orderDetails) {
        return new GetOrderResponse(orderDetails, null);
    }

    public static GetOrderResponse from(String message) {
        return new GetOrderResponse(null, message);
    }
}
