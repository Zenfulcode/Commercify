package com.zenfulcode.commercify.api.product.dto.request;

import com.zenfulcode.commercify.product.domain.valueobject.VariantOption;
import com.zenfulcode.commercify.shared.domain.model.Money;

import java.util.List;

public record ProductVariantRequest(
        int stock,
        Money price,
        String imageUrl,
        List<VariantOption> options
) {
}
