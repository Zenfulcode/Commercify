package com.zenfulcode.commercify.product.domain.service;

import com.zenfulcode.commercify.product.domain.event.LargeStockIncreaseEvent;
import com.zenfulcode.commercify.product.domain.event.LowStockEvent;
import com.zenfulcode.commercify.product.domain.event.StockCorrectionEvent;
import com.zenfulcode.commercify.product.domain.model.Product;
import com.zenfulcode.commercify.product.domain.policies.ProductInventoryPolicy;
import com.zenfulcode.commercify.product.domain.valueobject.InventoryAdjustment;
import com.zenfulcode.commercify.shared.domain.event.DomainEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DefaultProductInventoryPolicy implements ProductInventoryPolicy {
    private static final int LOW_STOCK_THRESHOLD = 5;
    private static final int REORDER_THRESHOLD = 10;

    private final DomainEventPublisher eventPublisher;

    @Override
    public void initializeInventory(Product product) {
        if (product.getStock() <= REORDER_THRESHOLD) {
            eventPublisher.publish(new LowStockEvent(product.getId(), product.getStock()));
        }
    }

    @Override
    public void handleStockIncrease(Product product, InventoryAdjustment adjustment) {
        if (adjustment.quantity() > 100) {
            eventPublisher.publish(new LargeStockIncreaseEvent(
                    product.getId(),
                    adjustment.quantity(),
                    adjustment.reason()
            ));
        }
    }

    @Override
    public void handleStockDecrease(Product product, InventoryAdjustment adjustment) {
        if (product.getStock() <= LOW_STOCK_THRESHOLD) {
            eventPublisher.publish(new LowStockEvent(product.getId(), product.getStock()));
        }
    }

    @Override
    public void handleStockCorrection(Product product, InventoryAdjustment adjustment) {
        eventPublisher.publish(new StockCorrectionEvent(
                product.getId(),
                adjustment.quantity(),
                adjustment.reason()
        ));
    }
}
