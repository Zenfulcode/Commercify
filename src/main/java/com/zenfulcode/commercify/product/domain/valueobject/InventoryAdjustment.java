package com.zenfulcode.commercify.product.domain.valueobject;


public record InventoryAdjustment(
        InventoryAdjustmentType type,
        int quantity,
        String reason
) {}
