package com.zenfulcode.commercify.commercify.api.requests.orders;

public record CreateOrderLineRequest(
        Integer productId,
        Integer variantId,
        Integer quantity
) {
}