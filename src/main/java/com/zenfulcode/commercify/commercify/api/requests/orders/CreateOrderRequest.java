package com.zenfulcode.commercify.commercify.api.requests.orders;

import com.zenfulcode.commercify.commercify.dto.AddressDTO;
import com.zenfulcode.commercify.commercify.dto.CustomerDetailsDTO;

import java.util.List;

public record CreateOrderRequest(
        String currency,
        CustomerDetailsDTO customerDetails,
        List<CreateOrderLineRequest> orderLines,
        AddressDTO shippingAddress,
        AddressDTO billingAddress
) {
}