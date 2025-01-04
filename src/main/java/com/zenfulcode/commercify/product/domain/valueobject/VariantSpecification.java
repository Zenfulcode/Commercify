package com.zenfulcode.commercify.product.domain.valueobject;

import com.zenfulcode.commercify.shared.domain.model.Money;

import java.util.List;

public record VariantSpecification(
        Integer stock,
        Money price,
        String imageUrl,
        List<VariantOption> options
) {
}