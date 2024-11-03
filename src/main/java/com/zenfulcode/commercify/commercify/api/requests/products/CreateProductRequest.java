package com.zenfulcode.commercify.commercify.api.requests.products;

public record CreateProductRequest(
        String name,
        String description,
        Integer stock,
        String imageUrl,
        Boolean active,
        CreatePriceRequest price
) {
}