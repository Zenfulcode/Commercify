package com.zenfulcode.commercify.product.application.command;

import com.zenfulcode.commercify.product.domain.valueobject.VariantSpecification;
import com.zenfulcode.commercify.shared.domain.model.Money;

import java.util.List;

public record CreateProductCommand(
        String name,
        String description,
        String imageUrl,
        int initialStock,
        Money price,
        List<VariantSpecification> variantSpecs
) {}