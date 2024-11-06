package com.zenfulcode.commercify.commercify.viewmodel;

import com.zenfulcode.commercify.commercify.dto.OrderLineDTO;

public record OrderLineViewModel(
        ProductViewModel product,
        Integer quantity
) {
    public static OrderLineViewModel from(ProductViewModel product, Integer quantity) {
        return new OrderLineViewModel(product, quantity);
    }

    public static OrderLineViewModel fromDTO(OrderLineDTO orderLineDTO) {
        return new OrderLineViewModel(
                ProductViewModel.fromDTO(orderLineDTO.getProduct()),
                orderLineDTO.getQuantity()
        );
    }
}
