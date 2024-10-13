package com.gostavdev.commercify.orderservice.api;

public record OrderLineRequest(
        Long productId,
        Integer quantity) {
}
