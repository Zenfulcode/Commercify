package com.zenfulcode.commercify.product.domain.service;

import com.zenfulcode.commercify.product.domain.exception.ProductValidationException;
import com.zenfulcode.commercify.product.domain.model.Product;
import com.zenfulcode.commercify.product.domain.model.ProductVariant;
import com.zenfulcode.commercify.product.domain.valueobject.ProductSpecification;
import com.zenfulcode.commercify.product.domain.valueobject.VariantSpecification;
import com.zenfulcode.commercify.shared.domain.model.Money;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductFactory {
    private final SkuGenerator skuGenerator;
    private final DefaultProductPricingPolicy pricingPolicy;
    private final DefaultProductInventoryPolicy inventoryPolicy;

    public Product createProduct(ProductSpecification spec) {
        // Validate specification
        validateSpecification(spec);

        // Create base product
        Product product = Product.create(spec.name(), spec.description(), spec.imageUrl(), spec.initialStock(), spec.price());

        // Apply default policies
        pricingPolicy.applyDefaultPricing(product);
        inventoryPolicy.initializeInventory(product);

        // Create variants if specified
        if (spec.hasVariants()) {
            createVariants(product, spec.variantSpecs());
        }

        return product;
    }

    public void createVariants(Product product, List<VariantSpecification> specs) {
        for (VariantSpecification spec : specs) {
            validateVariantSpecification(spec);

            String sku = skuGenerator.generateSku(product, spec);
            Money variantPrice = pricingPolicy.calculateVariantPrice(product, spec);

            ProductVariant variant = ProductVariant.create(sku, spec.stock(), variantPrice, spec.imageUrl());

            // Add variant options
            spec.options().forEach(option ->
                    variant.addOption(option.name(), option.value())
            );

            product.addVariant(variant);
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

    private void validateSpecification(ProductSpecification spec) {
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
}
