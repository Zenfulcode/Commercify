package com.zenfulcode.commercify.api.product.dto.response;

import com.zenfulcode.commercify.shared.domain.model.Money;

public record ProductSummaryResponse(
        String id,
        String name,
        String description,
        String imageUrl,
        Money price,
        int stock,
        boolean isActive
) {
}
