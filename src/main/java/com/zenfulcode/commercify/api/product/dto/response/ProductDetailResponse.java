package com.zenfulcode.commercify.api.product.dto.response;

import java.util.List;

public record ProductDetailResponse(
        String id,
        String name,
        String description,
        int stock,
        ProductSummaryResponse.ProductPriceResponse price,
        boolean active,
        List<ProductVariantSummaryResponse> variants
) {
}
