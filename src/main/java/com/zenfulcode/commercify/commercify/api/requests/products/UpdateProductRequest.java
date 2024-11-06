package com.zenfulcode.commercify.commercify.api.requests.products;

public record UpdateProductRequest(
        String name,
        String description,
        Integer stock,
        String imageUrl,
        Boolean active,
        UpdatePriceRequest price
) {
}