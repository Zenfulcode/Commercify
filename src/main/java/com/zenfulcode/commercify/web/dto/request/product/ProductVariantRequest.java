package com.zenfulcode.commercify.web.dto.request.product;

import java.util.List;

public record ProductVariantRequest(
        String sku,
        Integer stock,
        String imageUrl,
        Double unitPrice,
        List<CreateVariantOptionRequest> options
) {
}