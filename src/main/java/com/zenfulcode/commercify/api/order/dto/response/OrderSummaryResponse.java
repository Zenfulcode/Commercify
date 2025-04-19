package com.zenfulcode.commercify.api.order.dto.response;

import com.zenfulcode.commercify.shared.domain.model.Money;

import java.time.Instant;

public record OrderSummaryResponse(
        String id,
        String userId,
        String customerName,
        String status,
        int orderLineAmount,
        Money totalAmount,
        Instant createdAt
) {
}
