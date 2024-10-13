package com.gostavdev.commercify.orderservice.dto.mappers;

import com.gostavdev.commercify.orderservice.dto.OrderLineDTO;
import com.gostavdev.commercify.orderservice.model.OrderLine;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
@AllArgsConstructor
public class OrderLineDTOMapper implements Function<OrderLine, OrderLineDTO> {
    @Override
    public OrderLineDTO apply(OrderLine orderLine) {
        return OrderLineDTO.builder()
                .quantity(orderLine.getQuantity())
                .productId(orderLine.getProductId())
                .stripeProductId(orderLine.getStripeProductId())
                .unitPrice(orderLine.getUnitPrice())
                .build();
    }
}
