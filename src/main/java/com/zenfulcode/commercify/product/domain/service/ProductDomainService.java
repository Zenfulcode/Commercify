package com.zenfulcode.commercify.product.domain.service;

import com.zenfulcode.commercify.product.domain.exception.ProductValidationException;
import com.zenfulcode.commercify.product.domain.exception.VariantNotFoundException;
import com.zenfulcode.commercify.product.domain.model.*;
import com.zenfulcode.commercify.product.domain.policies.ProductInventoryPolicy;
import com.zenfulcode.commercify.product.domain.policies.ProductPricingPolicy;
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
    private final SkuGenerator skuGenerator;
    private final ProductFactory productFactory;

    /**
     * Creates a new product with validation and enrichment
     */
    public Product createProduct(ProductSpecification spec) {
        validateProductSpecification(spec);

        Product product = productFactory.createProduct(spec);

        // Apply any default product policies
        pricingPolicy.applyDefaultPricing(product);
        inventoryPolicy.initializeInventory(product);

        // Create variants if specified
        if (spec.hasVariants()) {
            createProductVariants(product, spec.variantSpecs());
        }

        return product;
    }

    /**
     * Handles complex variant creation logic
     */
    public void createProductVariants(Product product, List<VariantSpecification> variantSpecs) {
        for (VariantSpecification spec : variantSpecs) {
            validateVariantSpecification(spec);

            String sku = skuGenerator.generateSku(product, spec);
            Money variantPrice = pricingPolicy.calculateVariantPrice(product, spec);

            ProductVariant variant = ProductVariant.create(
                    sku,
                    spec.stock(),
                    variantPrice
            );

            // Add variant options
            spec.options().forEach(option ->
                    variant.addOption(option.name(), option.value())
            );

            product.addVariant(variant);
        }
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

    private void validateProductSpecification(ProductSpecification spec) {
        List<String> violations = new ArrayList<>();

        if (spec.name() == null || spec.name().isBlank()) {
            violations.add("Product name is required");
        }
        if (spec.price() == null || spec.price().isNegative()) {
            violations.add("Valid product price is required");
        }
        if (spec.initialStock() < 0) {
            violations.add("Initial stock cannot be negative");
        }

        if (!violations.isEmpty()) {
            throw new ProductValidationException(violations);
        }
    }

    private void validateVariantSpecification(VariantSpecification spec) {
        List<String> violations = new ArrayList<>();

        if (spec.options() == null || spec.options().isEmpty()) {
            violations.add("Variant must have at least one option");
        }
        if (spec.stock() != null && spec.stock() < 0) {
            violations.add("Variant stock cannot be negative");
        }

        if (!violations.isEmpty()) {
            throw new ProductValidationException(violations);
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

    }
}



