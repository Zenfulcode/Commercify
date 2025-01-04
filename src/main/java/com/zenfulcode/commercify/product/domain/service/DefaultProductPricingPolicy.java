package com.zenfulcode.commercify.product.domain.service;

import com.zenfulcode.commercify.product.domain.exception.InvalidPriceException;
import com.zenfulcode.commercify.product.domain.model.Product;
import com.zenfulcode.commercify.product.domain.model.ProductVariant;
import com.zenfulcode.commercify.product.domain.valueobject.VariantSpecification;
import com.zenfulcode.commercify.shared.domain.model.Money;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class DefaultProductPricingPolicy implements ProductPricingPolicy {
    private static final BigDecimal MAXIMUM_VARIANT_PRICE_MULTIPLIER = new BigDecimal("3.0");
    private static final BigDecimal MINIMUM_MARGIN_PERCENTAGE = new BigDecimal("0.10");

    @Override
    public void applyDefaultPricing(Product product) {
        // Apply minimum margin check
        if (product.getPrice().isLessThan(calculateMinimumPrice(product))) {
            throw new InvalidPriceException("Price does not meet minimum margin requirements");
        }
    }

    @Override
    public Money calculateVariantPrice(Product product, VariantSpecification spec) {
        if (spec.price() == null) {
            return product.getPrice();
        }

        // Validate variant price is not too high compared to base price
        if (spec.price().getAmount().divide(product.getPrice().getAmount(), 2, RoundingMode.HALF_UP)
                .compareTo(MAXIMUM_VARIANT_PRICE_MULTIPLIER) > 0) {
            throw new InvalidPriceException("Variant price cannot exceed 3x the base product price");
        }

        return spec.price();
    }


    // TODO: Rework this method
    @Override
    public Money validateAndAdjustPrice(Product product, ProductVariant variant, Money newPrice) {
        // Ensure variant price meets minimum margin
        if (newPrice.isLessThan(calculateMinimumPrice(product))) {
            throw new InvalidPriceException("Variant price does not meet minimum margin requirements");
        }

        return newPrice;
    }

    //    TODO: Rework this method
    private Money calculateMinimumPrice(Product product) {
        return product.getPrice().multiply(MINIMUM_MARGIN_PERCENTAGE);
    }
}
