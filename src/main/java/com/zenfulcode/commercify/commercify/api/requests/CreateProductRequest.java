package com.zenfulcode.commercify.commercify.api.requests;

public record CreateProductRequest(
        String name,
        String description,
        Double unitPrice,
        String currency,
        Integer stock,
        String imageUrl) {
}
