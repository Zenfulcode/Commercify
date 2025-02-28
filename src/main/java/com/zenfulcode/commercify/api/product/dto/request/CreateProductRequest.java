package com.zenfulcode.commercify.api.product.dto.request;

import com.zenfulcode.commercify.shared.domain.model.Money;

import java.util.List;

public record CreateProductRequest(
        String name,
        String description,
        String imageUrl,
        Integer initialStock,
        Money price,
        List<CreateVariantRequest> variants
) {
}
