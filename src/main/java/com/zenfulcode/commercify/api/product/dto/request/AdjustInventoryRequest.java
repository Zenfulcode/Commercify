package com.zenfulcode.commercify.api.product.dto.request;

public record AdjustInventoryRequest(
        String type,
        Integer quantity,
        String reason
) {
}
