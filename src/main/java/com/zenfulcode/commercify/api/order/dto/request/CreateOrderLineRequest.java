package com.zenfulcode.commercify.api.order.dto.request;


public record CreateOrderLineRequest(
        String productId,
        String variantId,
        int quantity
) {
}
