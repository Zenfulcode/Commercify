package com.zenfulcode.commercify.api.product.dto.response;

import com.zenfulcode.commercify.product.domain.valueobject.ProductId;

import java.util.List;

public record ProductDetailResponse(
        ProductId id,
        String name,
        String description,
        int stock,
        ProductSummaryResponse.ProductPriceResponse price,
        boolean active,
        List<ProductVariantSummaryResponse> variants
) {
}
