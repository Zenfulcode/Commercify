package com.zenfulcode.commercify.api.order.dto.response;

public record MoneyResponse(
        double amount,
        String currency
) {
}
