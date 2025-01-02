package com.zenfulcode.commercify.product.domain.valueobject;

import com.zenfulcode.commercify.shared.domain.model.Money;

public record VariantPriceUpdate(
        String sku,
        Money newPrice
) {}