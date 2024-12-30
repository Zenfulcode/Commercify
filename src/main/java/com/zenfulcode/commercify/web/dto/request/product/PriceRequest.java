package com.zenfulcode.commercify.web.dto.request.product;

public record PriceRequest(
        String currency,
        Double amount
) {
}