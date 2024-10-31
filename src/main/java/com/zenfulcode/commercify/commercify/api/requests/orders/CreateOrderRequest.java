package com.zenfulcode.commercify.commercify.api.requests.orders;

import java.util.List;

public record CreateOrderRequest(
        Long userId,
        String currency,
        List<CreateOrderLineRequest> orderLines
) {
}