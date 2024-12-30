package com.zenfulcode.commercify.web.dto.request.product;

import java.util.List;

public record ProductRequest(
        String name,
        String description,
        Integer stock,
        String imageUrl,
        Boolean active,
        PriceRequest price,
        List<ProductVariantRequest> variants
) {
}