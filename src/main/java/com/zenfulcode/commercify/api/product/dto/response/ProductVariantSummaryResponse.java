package com.zenfulcode.commercify.api.product.dto.response;

import java.util.List;

public record ProductVariantSummaryResponse(
        String id,
        String sku,
        List<VariantOptionResponse> options,
        ProductSummaryResponse.ProductPriceResponse price,
        int stock
) {
    public record VariantOptionResponse(
            String name,
            String value
    ) {
    }
}
