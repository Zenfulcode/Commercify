package com.zenfulcode.commercify.api.product.dto.request;

import java.util.List;

public record CreateProductRequest(
        String name,
        String description,
        int initialStock,
        PriceRequest price,
        List<CreateVariantRequest> variants
) {
}
