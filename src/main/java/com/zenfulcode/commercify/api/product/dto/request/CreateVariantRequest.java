package com.zenfulcode.commercify.api.product.dto.request;

import java.util.List;

public record CreateVariantRequest(
        Integer stock,
        PriceRequest price,
        String imageUrl,
        List<VariantOptionRequest> options
) {
}
