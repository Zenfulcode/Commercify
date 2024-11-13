package com.zenfulcode.commercify.commercify.api.requests.products;

public record CreateVariantOptionRequest(
        String name,
        String value
) {
}