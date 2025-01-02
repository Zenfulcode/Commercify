package com.zenfulcode.commercify.product.domain.service;

import com.zenfulcode.commercify.order.domain.repository.OrderLineRepository;
import com.zenfulcode.commercify.product.domain.exception.VariantNotFoundException;
import com.zenfulcode.commercify.product.domain.model.Product;
import com.zenfulcode.commercify.product.domain.model.ProductVariant;
import com.zenfulcode.commercify.product.domain.valueobject.*;
import com.zenfulcode.commercify.shared.domain.model.Money;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductDomainService {
    private final OrderLineRepository orderLineRepository;
    private final ProductInventoryPolicy inventoryPolicy;
    private final ProductPricingPolicy pricingPolicy;
    private final ProductFactory productFactory;

    /**
     * Creates a new product with validation and enrichment
     */
    public Product createProduct(ProductSpecification spec) {
        return productFactory.createProduct(spec);
    }

    /**
     * Handles complex variant creation logic
     */
    public void createProductVariants(Product product, List<VariantSpecification> variantSpecs) {
        productFactory.createVariants(product, variantSpecs);
    }

    /**
     * Validates if a product can be deleted
     */
    public ProductDeletionValidation validateProductDeletion(Product product) {
        List<String> issues = new ArrayList<>();

        // Check for active orders
        if (orderLineRepository.hasActiveOrders(product.getId())) {
            issues.add("Product has active orders");
        }

        // Check variants
        for (ProductVariant variant : product.getVariants()) {
            if (orderLineRepository.hasActiveOrdersForVariant(variant.getId())) {
                issues.add("Variant " + variant.getSku() + " has active orders");
            }
        }

        // Check inventory
        if (product.getStock() > 0) {
            issues.add("Product has remaining stock of " + product.getStock() + " units");
        }

        return new ProductDeletionValidation(issues.isEmpty(), issues);
    }

    /**
     * Updates product stock based on complex business rules
     */
    public void adjustInventory(Product product, InventoryAdjustment adjustment) {
        validateInventoryAdjustment(adjustment);

        switch (adjustment.type()) {
            case STOCK_ADDITION -> {
                product.addStock(adjustment.quantity());
                inventoryPolicy.handleStockIncrease(product, adjustment);
            }
            case STOCK_REMOVAL -> {
                product.removeStock(adjustment.quantity());
                inventoryPolicy.handleStockDecrease(product, adjustment);
            }
            case STOCK_CORRECTION -> {
                product.updateStock(adjustment.quantity());
                inventoryPolicy.handleStockCorrection(product, adjustment);
            }
        }
    }

    /**
     * Handles variant pricing updates with validation
     */
    public void updateVariantPrices(Product product, List<VariantPriceUpdate> updates) {
        validatePriceUpdates(updates);

        for (VariantPriceUpdate update : updates) {
            ProductVariant variant = product.findVariantBySku(update.sku())
                    .orElseThrow(() -> new VariantNotFoundException(update.sku()));

            Money newPrice = pricingPolicy.validateAndAdjustPrice(
                    product,
                    variant,
                    update.newPrice()
            );

            variant.updatePrice(newPrice);
        }
    }

    private void validateInventoryAdjustment(InventoryAdjustment adjustment) {
        if (adjustment.quantity() < 0) {
            throw new IllegalArgumentException("Adjustment quantity cannot be negative");
        }
    }

    private void validatePriceUpdates(List<VariantPriceUpdate> updates) {
        updates.forEach(update -> {
            if (update.newPrice().isNegative()) {
                throw new IllegalArgumentException(
                        "Price cannot be negative for variant: " + update.sku()
                );
            }
        });
    }

    public void updateProduct(Product product, ProductUpdateSpec productUpdateSpec) {
        // TODO: Implement product update logic
    }
}



