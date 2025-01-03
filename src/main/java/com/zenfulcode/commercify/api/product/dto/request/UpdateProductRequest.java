package com.zenfulcode.commercify.api.product.dto.request;

public record UpdateProductRequest(
        String name,
        String description,
        Integer stock,
        PriceRequest price,
        Boolean active
) {
}