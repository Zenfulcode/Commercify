package com.zenfulcode.commercify.api.order.dto.request;

import com.zenfulcode.commercify.user.domain.valueobject.UserId;

import java.util.List;

public record CreateOrderRequest(
        String userId,
        String currency,
        CustomerDetailsRequest customerDetails,
        AddressRequest shippingAddress,
        AddressRequest billingAddress,
        List<CreateOrderLineRequest> orderLines
) {
    public UserId getUserId() {
        return UserId.of(userId);
    }
}
