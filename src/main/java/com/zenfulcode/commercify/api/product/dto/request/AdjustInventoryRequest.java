package com.zenfulcode.commercify.api.product.dto.request;

public record AdjustInventoryRequest(
        String type,
        int quantity,
        String reason
) {
}
