package com.zenfulcode.commercify.commercify.api.requests.products;

public record UpdatePriceRequest(
        String currency,
        Double amount
) {
}