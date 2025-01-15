package com.zenfulcode.commercify.commercify.dto.mapper;

import com.zenfulcode.commercify.commercify.dto.OrderDTO;
import com.zenfulcode.commercify.commercify.entity.OrderEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class OrderMapper implements Function<OrderEntity, OrderDTO> {

    @Override
    public OrderDTO apply(OrderEntity order) {
        return OrderDTO.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .orderStatus(order.getStatus())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .shippingCost(order.getShippingCost() != null ? order.getShippingCost() : 0.0)
                .currency(order.getCurrency() != null ? order.getCurrency() : null)
                .subTotal(order.getSubTotal() != null ? order.getSubTotal() : 0.0)
                .orderLinesAmount(order.getOrderLines() != null ? order.getOrderLines().size() : 0)
                .build();
    }
}