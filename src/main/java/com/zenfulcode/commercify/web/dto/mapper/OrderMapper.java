package com.zenfulcode.commercify.web.dto.mapper;

import com.zenfulcode.commercify.web.dto.common.OrderDTO;
import com.zenfulcode.commercify.domain.model.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class OrderMapper implements Function<Order, OrderDTO> {

    @Override
    public OrderDTO apply(Order order) {
        return OrderDTO.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .orderStatus(order.getStatus())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .currency(order.getCurrency() != null ? order.getCurrency() : null)
                .totalAmount(order.getTotalAmount() != null ? order.getTotalAmount() : 0.0)
                .orderLinesAmount(order.getOrderLines() != null ? order.getOrderLines().size() : 0)
                .build();
    }
}