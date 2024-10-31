package com.zenfulcode.commercify.commercify.viewmodel;

import com.zenfulcode.commercify.commercify.dto.OrderDetailsDTO;

import java.util.List;

public record OrderDetailsViewModel(
        Long id,
        OrderViewModel order,
        List<OrderLineViewModel> orderLines
) {
    public static OrderDetailsViewModel fromDTO(OrderDetailsDTO orderDetailsDTO) {
        return new OrderDetailsViewModel(
                orderDetailsDTO.getOrder().getId(),
                OrderViewModel.fromDTO(orderDetailsDTO.getOrder()),
                orderDetailsDTO.getOrderLines().stream().map(OrderLineViewModel::fromDTO).toList()
        );
    }
}
