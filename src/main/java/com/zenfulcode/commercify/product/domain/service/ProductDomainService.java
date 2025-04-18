package com.zenfulcode.commercify.product.domain.service;

import com.zenfulcode.commercify.order.domain.repository.OrderLineRepository;
import com.zenfulcode.commercify.product.domain.exception.InvalidPriceException;
import com.zenfulcode.commercify.product.domain.exception.ProductNotFoundException;
import com.zenfulcode.commercify.product.domain.exception.ProductValidationException;
import com.zenfulcode.commercify.product.domain.exception.VariantNotFoundException;
import com.zenfulcode.commercify.product.domain.model.Product;
import com.zenfulcode.commercify.product.domain.model.ProductVariant;
import com.zenfulcode.commercify.product.domain.repository.ProductRepository;
import com.zenfulcode.commercify.product.domain.valueobject.*;
import com.zenfulcode.commercify.shared.domain.model.Money;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ProductDomainService {
    private final OrderLineRepository orderLineRepository;
    private final ProductRepository productRepository;
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
        for (ProductVariant variant : product.getProductVariants()) {
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

    public void updateProduct(Product product, ProductUpdateSpec updateSpec) {
        // Validate the product is not null
        Objects.requireNonNull(product, "Product cannot be null");
        Objects.requireNonNull(updateSpec, "Update specification cannot be null");

        List<String> violations = new ArrayList<>();

        // Update name if specified
        if (updateSpec.hasNameUpdate()) {
            if (updateSpec.name() == null || updateSpec.name().isBlank()) {
                violations.add("Product name cannot be empty");
            } else {
                product.setName(updateSpec.name());
            }
        }

        // Update description if specified
        if (updateSpec.hasDescriptionUpdate()) {
            product.setDescription(updateSpec.description());
        }

        // Update stock if specified
        if (updateSpec.hasStockUpdate()) {
            if (updateSpec.stock() < 0) {
                violations.add("Stock cannot be negative");
            } else {
                InventoryAdjustment adjustment = new InventoryAdjustment(
                        InventoryAdjustmentType.STOCK_CORRECTION,
                        updateSpec.stock(),
                        "Stock updated through product update"
                );
                this.adjustInventory(product, adjustment);
            }
        }

        // Update price if specified
        if (updateSpec.hasPriceUpdate()) {
            if (updateSpec.price() == null || updateSpec.price().isNegative()) {
                violations.add("Price must be non-negative");
            } else {
                try {
                    pricingPolicy.applyDefaultPricing(product);
                    product.updatePrice(updateSpec.price());
                } catch (InvalidPriceException e) {
                    violations.add(e.getMessage());
                }
            }
        }

        // Update active status if specified
        if (updateSpec.hasActiveUpdate()) {
            if (updateSpec.active()) {
                product.activate();
            } else {
                product.deactivate();
            }
        }

        // If there are any violations, throw an exception
        if (!violations.isEmpty()) {
            throw new ProductValidationException(violations);
        }

        if (updateSpec.hasStockUpdate()) {
            inventoryPolicy.initializeInventory(product);
        }
    }

    public Product getProductById(ProductId productId) {
        return productRepository.findById(productId).orElseThrow(
                () -> new ProductNotFoundException(productId)
        );
    }

    public List<Product> getAllProductsById(Collection<ProductId> productIds) {
        List<Product> products = productRepository.findAllById(productIds);

        if (products.size() != productIds.size()) {
            for (ProductId prodId : productIds) {
                if (products.stream().noneMatch(p -> p.getId().equals(prodId))) {
                    throw new ProductNotFoundException(prodId);
                }
            }
        }

        return products;
    }

    public int countNewProductsInPeriod(LocalDate startDate, LocalDate endDate) {
        Instant start = startDate.atStartOfDay().toInstant(ZoneOffset.from(ZoneOffset.UTC));
        Instant end = endDate.atTime(23, 59).toInstant(ZoneOffset.UTC);

        return productRepository.findNewProducts(start, end);
    }
}