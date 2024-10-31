package com.zenfulcode.commercify.commercify.api.requests;

public record UpdatePriceRequest(
        Long priceId,          // null for new prices
        String currency,
        Double amount,
        Boolean isDefault,
        Boolean active
) {
}