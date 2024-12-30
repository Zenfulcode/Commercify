package com.zenfulcode.commercify.web.dto.request.order;

import com.zenfulcode.commercify.web.dto.common.AddressDTO;
import com.zenfulcode.commercify.web.dto.common.CustomerDetailsDTO;

import java.util.List;

public record CreateOrderRequest(
        String currency,
        CustomerDetailsDTO customerDetails,
        List<CreateOrderLineRequest> orderLines,
        AddressDTO shippingAddress,
        AddressDTO billingAddress
) {
}