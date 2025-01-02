package com.zenfulcode.commercify.product.application.command;

import com.zenfulcode.commercify.product.domain.valueobject.ProductId;
import com.zenfulcode.commercify.product.domain.valueobject.VariantSpecification;

import java.util.List;

public record AddProductVariantsCommand(
        ProductId productId,
        List<VariantSpecification> variantSpecs
) {
}
