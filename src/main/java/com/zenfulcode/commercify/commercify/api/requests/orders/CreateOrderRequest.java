package com.zenfulcode.commercify.commercify.api.requests.orders;

import java.util.List;

public record CreateOrderRequest(
        String currency,
        List<CreateOrderLineRequest> orderLines
) {
}