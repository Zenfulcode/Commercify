package com.gostavdev.commercify.orderservice.api;

import java.util.List;

public record CreateOrderRequest(
        Long userId,
        List<OrderLineRequest> orderLines) {
}
