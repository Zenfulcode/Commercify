package com.zenfulcode.commercify.commercify.dto.mapper;

import com.zenfulcode.commercify.commercify.dto.OrderLineDTO;
import com.zenfulcode.commercify.commercify.entity.OrderLineEntity;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
@AllArgsConstructor
public class OrderLineMapper implements Function<OrderLineEntity, OrderLineDTO> {
    @Override
    public OrderLineDTO apply(OrderLineEntity orderLine) {
        return OrderLineDTO.builder()
                .id(orderLine.getId())
                .quantity(orderLine.getQuantity())
                .productId(orderLine.getProductId())
                .unitPrice(orderLine.getUnitPrice())
                .build();
    }
}
