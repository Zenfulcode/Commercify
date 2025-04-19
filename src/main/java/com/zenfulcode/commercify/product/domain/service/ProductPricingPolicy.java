package com.zenfulcode.commercify.product.domain.service;

import com.zenfulcode.commercify.product.domain.model.Product;
import com.zenfulcode.commercify.product.domain.model.ProductVariant;
import com.zenfulcode.commercify.product.domain.valueobject.VariantSpecification;
import com.zenfulcode.commercify.shared.domain.model.Money;

public interface ProductPricingPolicy {
    void applyDefaultPricing(Product product);
    Money calculateVariantPrice(Product product, VariantSpecification spec);
    Money validateAndAdjustPrice(Product product, ProductVariant variant, Money newPrice);
}

