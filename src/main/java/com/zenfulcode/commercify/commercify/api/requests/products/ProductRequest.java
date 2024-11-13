package com.zenfulcode.commercify.commercify.api.requests.products;

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