package com.zenfulcode.commercify.commercify.api.requests.orders;

public record CreateOrderLineRequest(
        Long productId,
        Integer quantity
) {
}