package com.zenfulcode.commercify.api.product.dto.response;

import com.zenfulcode.commercify.shared.domain.model.Money;

import java.util.List;

public record ProductDetailResponse(
        String id,
        String name,
        String description,
        String imageUrl,
        int stock,
        Money price,
        boolean active,
        List<ProductVariantSummaryResponse> variants
) {

}
