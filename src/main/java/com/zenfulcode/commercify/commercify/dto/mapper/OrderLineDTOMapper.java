package com.zenfulcode.commercify.commercify.dto.mapper;

import com.zenfulcode.commercify.commercify.dto.OrderLineDTO;
import com.zenfulcode.commercify.commercify.entity.OrderLineEntity;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
@AllArgsConstructor
public class OrderLineDTOMapper implements Function<OrderLineEntity, OrderLineDTO> {
    @Override
    public OrderLineDTO apply(OrderLineEntity orderLine) {
        return OrderLineDTO.builder()
                .quantity(orderLine.getQuantity())
                .productId(orderLine.getProductId())
                .stripeProductId(orderLine.getStripeProductId())
                .unitPrice(orderLine.getUnitPrice())
                .build();
    }
}
