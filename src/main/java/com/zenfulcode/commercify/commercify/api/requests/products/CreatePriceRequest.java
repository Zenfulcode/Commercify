package com.zenfulcode.commercify.commercify.api.requests.products;

public record CreatePriceRequest(
        String currency,
        Double amount,
        Boolean isDefault,
        Boolean active
) {
}
