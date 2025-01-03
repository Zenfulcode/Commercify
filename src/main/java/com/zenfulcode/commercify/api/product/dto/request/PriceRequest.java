package com.zenfulcode.commercify.api.product.dto.request;

public record PriceRequest(
        double amount,
        String currency
) {
}
