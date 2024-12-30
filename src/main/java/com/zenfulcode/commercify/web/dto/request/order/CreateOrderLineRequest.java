package com.zenfulcode.commercify.web.dto.request.order;

public record CreateOrderLineRequest(
        Long productId,
        Long variantId,
        Integer quantity
) {
}