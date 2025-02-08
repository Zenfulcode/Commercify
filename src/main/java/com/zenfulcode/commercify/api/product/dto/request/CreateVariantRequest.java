package com.zenfulcode.commercify.api.product.dto.request;

import com.zenfulcode.commercify.shared.domain.model.Money;

import java.util.List;

public record CreateVariantRequest(
        Integer stock,
        Money price,
        String imageUrl,
        List<VariantOptionRequest> options
) {
}
