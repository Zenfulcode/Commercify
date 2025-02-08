package com.zenfulcode.commercify.api.product.dto.request;

import com.zenfulcode.commercify.shared.domain.model.Money;

public record VariantPriceUpdateRequest(
        String sku,
        Money price
) {
}
