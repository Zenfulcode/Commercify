package com.zenfulcode.commercify.api.order.dto.request;

import java.util.List;

public record CreateOrderRequest(
        String userId,
        String currency,
        CustomerDetailsRequest customerDetails,
        AddressRequest shippingAddress,
        AddressRequest billingAddress,
        List<CreateOrderLineRequest> orderLines
) {
}
