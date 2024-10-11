package com.gostavdev.commercify.orderservice.dto;


public record OrderLineDTO(
        Long productId,
        String stripeProductId,
        Integer quantity,
        Double unitPrice,
        ProductDto product) {
}
