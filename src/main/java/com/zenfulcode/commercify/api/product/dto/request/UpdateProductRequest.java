package com.zenfulcode.commercify.api.product.dto.request;

import com.zenfulcode.commercify.shared.domain.model.Money;

public record UpdateProductRequest(
        String name,
        String description,
        String imageUrl,
        Integer stock,
        Money price,
        Boolean active
) {
}