package com.zenfulcode.commercify.commercify.api.requests.orders;

import com.zenfulcode.commercify.commercify.dto.AddressDTO;

import java.util.List;

public record CreateOrderRequest(
        String currency,
        List<CreateOrderLineRequest> orderLines,
        AddressDTO shippingAddress,
        AddressDTO billingAddress
) {
}