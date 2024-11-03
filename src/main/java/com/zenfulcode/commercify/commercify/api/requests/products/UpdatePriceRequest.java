package com.zenfulcode.commercify.commercify.api.requests.products;

public record UpdatePriceRequest(
        Long priceId,          // null for new prices
        String currency,
        Double amount,
        Boolean active
) {
}