package com.zenfulcode.commercify.product.domain.valueobject;

import com.zenfulcode.commercify.shared.domain.model.Money;

import java.util.List;

public record ProductSpecification(
        String name,
        String description,
        int initialStock,
        Money price,
        List<VariantSpecification> variantSpecs
) {
    public boolean hasVariants() {
        return variantSpecs != null && !variantSpecs.isEmpty();
    }
}