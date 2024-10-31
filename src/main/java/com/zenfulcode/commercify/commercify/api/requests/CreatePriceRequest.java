package com.zenfulcode.commercify.commercify.api.requests;

public record CreatePriceRequest(
        String currency,
        Double amount,
        Boolean isDefault,
        Boolean active
) {
}
