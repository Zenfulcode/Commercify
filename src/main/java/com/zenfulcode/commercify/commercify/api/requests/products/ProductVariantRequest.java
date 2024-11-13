package com.zenfulcode.commercify.commercify.api.requests.products;

import java.util.List;

public record ProductVariantRequest(
        String sku,
        Integer stock,
        String imageUrl,
        PriceRequest price,
        List<CreateVariantOptionRequest> options
) {}