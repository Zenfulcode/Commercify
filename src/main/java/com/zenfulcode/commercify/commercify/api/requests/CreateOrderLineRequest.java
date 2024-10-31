package com.zenfulcode.commercify.commercify.api.requests;

public record CreateOrderLineRequest(
        Long productId,
        Integer quantity
) {
}