package com.zenfulcode.commercify.product.application.command;

import com.zenfulcode.commercify.product.domain.valueobject.ProductId;

public record DeactivateProductCommand(
        ProductId productId
) {
}