package com.zenfulcode.commercify.commercify.viewmodel;

import com.zenfulcode.commercify.commercify.OrderStatus;
import com.zenfulcode.commercify.commercify.dto.OrderDTO;

public record OrderViewModel(
        Long id,
        Long userId,
        PriceViewModel totalPrice,
        OrderStatus orderStatus
) {
    public static OrderViewModel fromDTO(OrderDTO orderDTO) {
        return new OrderViewModel(
                orderDTO.getId(),
                orderDTO.getUserId(),
                PriceViewModel.from(orderDTO.getCurrency(), orderDTO.getTotalAmount()),
                orderDTO.getOrderStatus()
        );
    }
}
