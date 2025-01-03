package com.zenfulcode.commercify.order.application.command;

import com.zenfulcode.commercify.order.domain.valueobject.Address;
import com.zenfulcode.commercify.order.domain.valueobject.CustomerDetails;
import com.zenfulcode.commercify.order.domain.valueobject.OrderLineDetails;
import com.zenfulcode.commercify.user.domain.valueobject.UserId;

import java.util.List;

public record CreateOrderCommand(
        UserId customerId,
        String currency,
        CustomerDetails customerDetails,
        Address shippingAddress,
        Address billingAddress,
        List<OrderLineDetails> orderLines
) {
}
