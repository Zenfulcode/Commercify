package com.zenfulcode.commercify.commercify.api.responses.orders;


import com.zenfulcode.commercify.commercify.OrderStatus;
import com.zenfulcode.commercify.commercify.dto.AddressDTO;
import com.zenfulcode.commercify.commercify.dto.OrderDTO;
import com.zenfulcode.commercify.commercify.dto.OrderDetailsDTO;
import com.zenfulcode.commercify.commercify.viewmodel.OrderLineViewModel;

import java.time.Instant;
import java.util.List;

public record GetOrderResponse(
        Long id,
        Long userId,
        String customerName,
        String customerEmail,
        AddressDTO shippingAddress,
        OrderStatus orderStatus,
        String currency,
        Double subTotal,
        Double shippingCost,
        Instant createdAt,
        Instant updatedAt,
        List<OrderLineViewModel> orderLines
) {
    public static GetOrderResponse from(OrderDetailsDTO orderDetails) {
        OrderDTO order = orderDetails.getOrder();
        return new GetOrderResponse(
                order.getId(),
                order.getUserId(),
                orderDetails.getCustomerDetails().getFirstName() + " " + orderDetails.getCustomerDetails().getLastName(),
                orderDetails.getCustomerDetails().getEmail(),
                orderDetails.getShippingAddress(),
                order.getOrderStatus(),
                order.getCurrency(),
                order.getSubTotal(),
                order.getShippingCost(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                orderDetails.getOrderLines().stream()
                        .map(OrderLineViewModel::fromDTO)
                        .toList()
        );
    }
}