package com.zenfulcode.commercify.product.application.command;

import com.zenfulcode.commercify.product.domain.valueobject.ProductId;

public record DeleteProductCommand(
        ProductId productId
) {
}