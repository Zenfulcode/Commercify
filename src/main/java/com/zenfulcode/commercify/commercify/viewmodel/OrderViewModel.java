package com.zenfulcode.commercify.commercify.viewmodel;

import com.zenfulcode.commercify.commercify.OrderStatus;
import com.zenfulcode.commercify.commercify.dto.OrderDTO;

import java.time.Instant;

public record OrderViewModel(
        Integer id,
        Integer userId,
        Double totalPrice,
        String currency,
        OrderStatus orderStatus,
        Instant createdAt
) {
    public static OrderViewModel fromDTO(OrderDTO orderDTO) {
        return new OrderViewModel(
                orderDTO.getId(),
                orderDTO.getUserId(),
                orderDTO.getTotalAmount(),
                orderDTO.getCurrency(),
                orderDTO.getOrderStatus(),
                orderDTO.getCreatedAt()
        );
    }
}
