package com.zenfulcode.commercify.api.order.dto.response;

import java.time.Instant;
import java.util.List;

public record OrderDetailsResponse(
        String id,
        String userId,
        String status,
        String currency,
        MoneyResponse totalAmount,
        List<OrderLineResponse> orderLines,
        CustomerDetailsResponse customerDetails,
        AddressResponse shippingAddress,
        AddressResponse billingAddress,
        Instant createdAt
) {}
