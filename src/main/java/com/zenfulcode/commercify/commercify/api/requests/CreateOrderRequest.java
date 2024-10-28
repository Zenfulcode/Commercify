package com.zenfulcode.commercify.commercify.api.requests;

import java.util.List;

public record CreateOrderRequest(
        Long userId,
        List<CreateOrderLineRequest> orderLines) {
}
