package com.zenfulcode.commercify.product.application.command;

import com.zenfulcode.commercify.product.domain.valueobject.InventoryAdjustmentType;
import com.zenfulcode.commercify.product.domain.valueobject.ProductId;

public record AdjustInventoryCommand(
        ProductId productId,
        InventoryAdjustmentType adjustmentType,
        int quantity,
        String reason
) {
}
