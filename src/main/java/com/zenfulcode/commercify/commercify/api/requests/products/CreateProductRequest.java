package com.zenfulcode.commercify.commercify.api.requests.products;

import java.util.List;

public record CreateProductRequest(
        String name,
        String description,
        Integer stock,
        String imageUrl,
        Boolean active,
        List<CreatePriceRequest> prices
) {
}