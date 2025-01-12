package com.zenfulcode.commercify.order.application.dto;

import com.zenfulcode.commercify.order.domain.model.Order;
import com.zenfulcode.commercify.order.domain.model.OrderStatus;
import com.zenfulcode.commercify.order.domain.valueobject.Address;
import com.zenfulcode.commercify.order.domain.valueobject.CustomerDetails;
import com.zenfulcode.commercify.order.domain.valueobject.OrderId;
import com.zenfulcode.commercify.shared.domain.model.Money;
import com.zenfulcode.commercify.user.domain.valueobject.UserId;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

public record OrderDetailsDTO(OrderId id,
                              UserId userId,
                              OrderStatus status,
                              String currency,
                              Money totalAmount,
                              List<OrderLineDTO> orderLines,
                              CustomerDetails customerDetails,
                              Address shippingAddress,
                              Address billingAddress,
                              Instant createdAt) {
    public static OrderDetailsDTO fromOrder(Order order) {
        return new OrderDetailsDTO(
                order.getId(),
                order.getUser().getId(),
                order.getStatus(),
                order.getCurrency(),
                order.getTotalAmount(),
                order.getOrderLines().stream()
                        .map(OrderLineDTO::fromOrderLine)
                        .collect(Collectors.toList()),
                order.getOrderShippingInfo().toCustomerDetails(),
                order.getOrderShippingInfo().toShippingAddress(),
                order.getOrderShippingInfo().toBillingAddress(),
                order.getCreatedAt()
        );
    }
}
