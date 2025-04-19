package com.zenfulcode.commercify.api.product.dto.response;

import com.zenfulcode.commercify.shared.domain.model.Money;

import java.util.List;

public record ProductVariantSummaryResponse(
        String id,
        String sku,
        List<VariantOptionResponse> options,
        Money price,
        int stock
) {
    public record VariantOptionResponse(
            String name,
            String value
    ) {
    }
}
