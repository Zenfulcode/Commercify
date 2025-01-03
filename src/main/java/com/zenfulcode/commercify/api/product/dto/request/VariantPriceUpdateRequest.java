package com.zenfulcode.commercify.api.product.dto.request;

public record VariantPriceUpdateRequest(
        String sku,
        PriceRequest price
) {
}
