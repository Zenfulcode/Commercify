package com.zenfulcode.commercify.api.order.dto.response;

import java.time.Instant;

public record OrderSummaryResponse(
        String id,
        String userId,
        String status,
        MoneyResponse totalAmount,
        Instant createdAt
) {
}
