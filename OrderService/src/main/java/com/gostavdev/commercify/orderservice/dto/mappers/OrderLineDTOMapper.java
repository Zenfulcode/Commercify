package com.gostavdev.commercify.orderservice.dto.mappers;

import com.gostavdev.commercify.orderservice.dto.OrderLineDTO;
import com.gostavdev.commercify.orderservice.feignclients.ProductsClient;
import com.gostavdev.commercify.orderservice.model.OrderLine;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
@AllArgsConstructor
public class OrderLineDTOMapper implements Function<OrderLine, OrderLineDTO> {
    private final ProductsClient productsClient;

    @Override
    public OrderLineDTO apply(OrderLine orderLine) {
        return new OrderLineDTO(
                orderLine.getProductId(),
                orderLine.getStripeProductId(),
                orderLine.getQuantity(),
                orderLine.getUnitPrice(),
                productsClient.getProductById(orderLine.getProductId())
        );
    }
}
