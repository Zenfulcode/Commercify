package com.zenfulcode.commercify.web.dto.mapper;

import com.zenfulcode.commercify.web.dto.common.OrderLineDTO;
import com.zenfulcode.commercify.domain.model.OrderLine;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
@AllArgsConstructor
public class OrderLineMapper implements Function<OrderLine, OrderLineDTO> {
    @Override
    public OrderLineDTO apply(OrderLine orderLine) {
        return OrderLineDTO.builder()
                .id(orderLine.getId())
                .quantity(orderLine.getQuantity())
                .productId(orderLine.getProductId())
                .unitPrice(orderLine.getUnitPrice())
                .build();
    }
}
