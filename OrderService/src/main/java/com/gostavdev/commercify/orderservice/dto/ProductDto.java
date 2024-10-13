package com.gostavdev.commercify.orderservice.dto;

public record ProductDto(
        Long productId,
        String stripeId,
        String name,
        String description,
        Double unitPrice,
        Integer stock) {
}
