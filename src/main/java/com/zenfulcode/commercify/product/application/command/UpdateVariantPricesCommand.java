package com.zenfulcode.commercify.product.application.command;

import com.zenfulcode.commercify.product.domain.valueobject.ProductId;
import com.zenfulcode.commercify.product.domain.valueobject.VariantPriceUpdate;

import java.util.List;

public record UpdateVariantPricesCommand(
        ProductId productId,
        List<VariantPriceUpdate> priceUpdates
) {
}