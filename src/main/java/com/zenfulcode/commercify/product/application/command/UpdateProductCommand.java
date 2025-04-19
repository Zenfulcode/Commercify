package com.zenfulcode.commercify.product.application.command;

import com.zenfulcode.commercify.product.domain.valueobject.ProductId;
import com.zenfulcode.commercify.product.domain.valueobject.ProductUpdateSpec;

public record UpdateProductCommand(
        ProductId productId,
        ProductUpdateSpec updateSpec
) {}
