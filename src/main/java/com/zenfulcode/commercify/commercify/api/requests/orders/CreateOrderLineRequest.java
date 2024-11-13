package com.zenfulcode.commercify.commercify.api.requests.orders;

public record CreateOrderLineRequest(
        Long productId,
        Long variantId,
        Integer quantity
) {
}