package com.zenfulcode.commercify.commercify.api.responses.orders;


import com.zenfulcode.commercify.commercify.viewmodel.OrderDetailsViewModel;

public record GetOrderResponse(OrderDetailsViewModel orderDetails, String message) {
    public static GetOrderResponse from(OrderDetailsViewModel orderDetails) {
        return new GetOrderResponse(orderDetails, null);
    }

    public static GetOrderResponse from(String message) {
        return new GetOrderResponse(null, message);
    }
}
