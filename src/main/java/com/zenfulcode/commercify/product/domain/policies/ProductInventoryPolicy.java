package com.zenfulcode.commercify.product.domain.policies;

import com.zenfulcode.commercify.product.domain.model.Product;
import com.zenfulcode.commercify.product.domain.valueobject.InventoryAdjustment;

public interface ProductInventoryPolicy {
    void initializeInventory(Product product);
    void handleStockIncrease(Product product, InventoryAdjustment adjustment);
    void handleStockDecrease(Product product, InventoryAdjustment adjustment);
    void handleStockCorrection(Product product, InventoryAdjustment adjustment);
}
