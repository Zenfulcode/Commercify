package com.zenfulcode.commercify.api.order.dto.response;

import com.zenfulcode.commercify.shared.domain.model.Money;

import java.time.Instant;
import java.util.List;

public record OrderDetailsResponse(
        String id,
        String userId,
        String status,
        Money totalAmount,
        List<OrderLineResponse> orderLines,
        CustomerDetailsResponse customerDetails,
        AddressResponse shippingAddress,
        AddressResponse billingAddress,
        Instant createdAt
) {
}
