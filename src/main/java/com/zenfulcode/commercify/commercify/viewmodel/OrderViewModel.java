package com.zenfulcode.commercify.commercify.viewmodel;

import com.zenfulcode.commercify.commercify.OrderStatus;
import com.zenfulcode.commercify.commercify.dto.OrderDTO;

public record OrderViewModel(
        Long id,
        Long userId,
        Double totalPrice,
        String currency,
        OrderStatus orderStatus
) {
    public static OrderViewModel fromDTO(OrderDTO orderDTO) {
        return new OrderViewModel(
                orderDTO.getId(),
                orderDTO.getUserId(),
                orderDTO.getTotalAmount(),
                orderDTO.getCurrency(),
                orderDTO.getOrderStatus()
        );
    }
}
