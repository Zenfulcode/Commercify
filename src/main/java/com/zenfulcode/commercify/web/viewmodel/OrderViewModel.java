package com.zenfulcode.commercify.web.viewmodel;

import com.zenfulcode.commercify.domain.enums.OrderStatus;
import com.zenfulcode.commercify.web.dto.common.OrderDTO;

import java.time.Instant;

public record OrderViewModel(
        long id,
        Long userId,
        int orderLinesAmount,
        double totalPrice,
        String currency,
        OrderStatus orderStatus,
        Instant createdAt
) {
    public static OrderViewModel fromDTO(OrderDTO orderDTO) {
        return new OrderViewModel(
                orderDTO.getId(),
                orderDTO.getUserId(),
                orderDTO.getOrderLinesAmount(),
                orderDTO.getTotalAmount(),
                orderDTO.getCurrency(),
                orderDTO.getOrderStatus(),
                orderDTO.getCreatedAt()
        );
    }
}
