package com.zenfulcode.commercify.commercify.api.requests.products;

import java.util.List;

public record UpdateProductRequest(
        String name,
        String description,
        Integer stock,
        String imageUrl,
        Boolean active,
        List<UpdatePriceRequest> prices
) {
}