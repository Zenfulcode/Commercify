package com.zenfulcode.commercify.commercify.api.responses.orders;


import com.zenfulcode.commercify.commercify.OrderStatus;
import com.zenfulcode.commercify.commercify.dto.OrderDTO;
import com.zenfulcode.commercify.commercify.dto.OrderDetailsDTO;
import com.zenfulcode.commercify.commercify.viewmodel.OrderLineViewModel;

import java.time.Instant;
import java.util.List;

public record GetOrderResponse(
        Long id,
        Long userId,
        OrderStatus orderStatus,
        String currency,
        Double totalAmount,
        Instant createdAt,
        Instant updatedAt,
        List<OrderLineViewModel> orderLines
) {
    public static GetOrderResponse from(OrderDetailsDTO orderDetails) {
        OrderDTO order = orderDetails.getOrder();
        return new GetOrderResponse(
                order.getId(),
                order.getUserId(),
                order.getOrderStatus(),
                order.getCurrency(),
                order.getTotalAmount(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                orderDetails.getOrderLines().stream()
                        .map(OrderLineViewModel::fromDTO)
                        .toList()
        );
    }
}