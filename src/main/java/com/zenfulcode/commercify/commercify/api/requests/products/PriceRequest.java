package com.zenfulcode.commercify.commercify.api.requests.products;

public record PriceRequest(
        String currency,
        Double amount
) {
}