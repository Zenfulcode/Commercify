package com.zenfulcode.commercify.api.order.dto.response;

public record OrderLineResponse(
        String id,
        String productId,
        String variantId,
        int quantity,
        MoneyResponse unitPrice,
        MoneyResponse total
) {
}