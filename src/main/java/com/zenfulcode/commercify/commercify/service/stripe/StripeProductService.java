package com.zenfulcode.commercify.commercify.service.stripe;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Price;
import com.stripe.model.PriceSearchResult;
import com.stripe.model.Product;
import com.stripe.param.*;
import com.zenfulcode.commercify.commercify.api.requests.products.PriceRequest;
import com.zenfulcode.commercify.commercify.entity.ProductEntity;
import com.zenfulcode.commercify.commercify.entity.ProductVariantEntity;
import com.zenfulcode.commercify.commercify.exception.StripeOperationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class StripeProductService {

    public String createStripeProductWithVariants(ProductEntity product) {
        validateStripeKey();

        try {
            // Create base product in Stripe
            Map<String, String> metadata = new HashMap<>();
            metadata.put("productId", product.getId().toString());

            ProductCreateParams params = ProductCreateParams.builder()
                    .setName(product.getName())
                    .setDescription(product.getDescription())
                    .setActive(product.getActive())
                    .putAllMetadata(metadata)
                    .build();

            Product stripeProduct = Product.create(params);

            // Create base price if provided
            if (product.getUnitPrice() != null) {
                PriceRequest basePriceRequest = new PriceRequest(
                        product.getCurrency(),
                        product.getUnitPrice()
                );
                String basePriceId = createStripePrice(product, basePriceRequest);
                product.setStripePriceId(basePriceId);
            }

            // Create prices for each variant
            for (ProductVariantEntity variant : product.getVariants()) {
                createStripeVariantPrice(stripeProduct.getId(), variant);
            }

            return stripeProduct.getId();
        } catch (StripeException e) {
            log.error("Failed to create Stripe product with variants", e);
            throw new StripeOperationException("Failed to create Stripe product with variants", e);
        }
    }

    public void createStripeVariantPrice(String stripeProductId, ProductVariantEntity variant) {
        validateStripeKey();

        try {
            // Create variant metadata
            Map<String, String> metadata = buildVariantMetadata(variant);

            // Convert price to cents for Stripe
            long amountInCents = (long) (variant.getPrice() * 100);

            // Create price for variant
            PriceCreateParams priceParams = PriceCreateParams.builder()
                    .setProduct(stripeProductId)
                    .setCurrency(variant.getCurrency().toLowerCase())
                    .setUnitAmount(amountInCents)
                    .putAllMetadata(metadata)
                    .setActive(true)
                    .build();

            Price stripePrice = Price.create(priceParams);
            variant.setStripePriceId(stripePrice.getId());

            log.info("Created Stripe price for variant: {}", variant.getSku());
        } catch (StripeException e) {
            log.error("Failed to create Stripe price for variant: {}", variant.getSku(), e);
            throw new StripeOperationException("Failed to create Stripe price for variant", e);
        }
    }

    public void updateStripeProduct(String stripeId, ProductEntity product) {
        validateStripeKey();

        try {
            Product stripeProduct = Product.retrieve(stripeId);

            ProductUpdateParams params = ProductUpdateParams.builder()
                    .setName(product.getName())
                    .setDescription(product.getDescription())
                    .setActive(product.getActive())
                    .build();

            stripeProduct.update(params);
            log.info("Updated Stripe product: {}", stripeId);
        } catch (StripeException e) {
            log.error("Failed to update Stripe product: {}", stripeId, e);
            throw new StripeOperationException("Failed to update Stripe product", e);
        }
    }

    public void updateStripeVariantPrice(ProductVariantEntity variant, PriceRequest request) {
        validateStripeKey();

        try {
            // Deactivate the old price
            if (variant.getStripePriceId() != null) {
                deactivatePrice(variant.getStripePriceId());
            }

            // Create metadata for the new price
            Map<String, String> metadata = buildVariantMetadata(variant);

            // Create new price
            long amountInCents = (long) (request.amount() * 100);
            PriceCreateParams priceParams = PriceCreateParams.builder()
                    .setProduct(variant.getProduct().getStripeId())
                    .setCurrency(request.currency().toLowerCase())
                    .setUnitAmount(amountInCents)
                    .putAllMetadata(metadata)
                    .setActive(true)
                    .build();

            Price newPrice = Price.create(priceParams);
            variant.setStripePriceId(newPrice.getId());

            log.info("Updated Stripe price for variant: {}", variant.getSku());
        } catch (StripeException e) {
            log.error("Failed to update Stripe price for variant: {}", variant.getSku(), e);
            throw new StripeOperationException("Failed to update Stripe price for variant", e);
        }
    }

    public void deactivateProduct(ProductEntity product) {
        validateStripeKey();

        try {
            // Deactivate all associated prices
            deactivateAllPrices(product.getStripeId());

            // Deactivate the product
            Product stripeProduct = Product.retrieve(product.getStripeId());
            ProductUpdateParams params = ProductUpdateParams.builder()
                    .setActive(false)
                    .build();

            stripeProduct.update(params);
            log.info("Deactivated Stripe product: {}", product.getStripeId());
        } catch (StripeException e) {
            log.error("Failed to deactivate Stripe product: {}", product.getStripeId(), e);
            throw new StripeOperationException("Failed to deactivate Stripe product", e);
        }
    }

    public void deactivateVariantPrice(ProductVariantEntity variant) {
        validateStripeKey();

        try {
            if (variant.getStripePriceId() != null) {
                deactivatePrice(variant.getStripePriceId());
                log.info("Deactivated Stripe price for variant: {}", variant.getSku());
            }
        } catch (StripeException e) {
            log.error("Failed to deactivate Stripe price for variant: {}", variant.getSku(), e);
            throw new StripeOperationException("Failed to deactivate Stripe price for variant", e);
        }
    }

    public void reactivateProduct(ProductEntity product) {
        validateStripeKey();

        try {
            // Reactivate the product
            Product stripeProduct = Product.retrieve(product.getStripeId());
            ProductUpdateParams params = ProductUpdateParams.builder()
                    .setActive(true)
                    .build();
            stripeProduct.update(params);

            // Reactivate the latest price for each variant
            reactivateAllPrices(product.getStripeId());

            log.info("Reactivated Stripe product: {}", product.getStripeId());
        } catch (StripeException e) {
            log.error("Failed to reactivate Stripe product: {}", product.getStripeId(), e);
            throw new StripeOperationException("Failed to reactivate Stripe product", e);
        }
    }

    // Helper methods
    private String createStripePrice(ProductEntity product, PriceRequest request) throws StripeException {
        long amountInCents = (long) (request.amount() * 100);
        PriceCreateParams params = PriceCreateParams.builder()
                .setProduct(product.getStripeId())
                .setCurrency(request.currency().toLowerCase())
                .setUnitAmount(amountInCents)
                .setActive(true)
                .build();

        Price stripePrice = Price.create(params);
        return stripePrice.getId();
    }

    private void deactivatePrice(String priceId) throws StripeException {
        Price price = Price.retrieve(priceId);
        PriceUpdateParams params = PriceUpdateParams.builder()
                .setActive(false)
                .build();
        price.update(params);
    }

    private void deactivateAllPrices(String productId) throws StripeException {
        PriceSearchParams params = PriceSearchParams.builder()
                .setQuery("product:'" + productId + "' AND active:'true'")
                .build();

        PriceSearchResult prices = Price.search(params);
        for (Price price : prices.getData()) {
            deactivatePrice(price.getId());
        }
    }

    private void reactivateAllPrices(String productId) throws StripeException {
        PriceSearchParams params = PriceSearchParams.builder()
                .setQuery("product:'" + productId + "' AND active:'false'")
                .build();

        PriceSearchResult prices = Price.search(params);
        for (Price price : prices.getData()) {
            Price.retrieve(price.getId()).update(
                    Map.of("active", true)
            );
        }
    }

    private Map<String, String> buildVariantMetadata(ProductVariantEntity variant) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("variantId", variant.getId().toString());
        metadata.put("sku", variant.getSku());

        // Add variant options to metadata
        variant.getOptions().forEach(option ->
                metadata.put("option_" + option.getName().toLowerCase(), option.getValue())
        );

        return metadata;
    }

    private void validateStripeKey() {
        if (Stripe.apiKey == null || Stripe.apiKey.isBlank()) {
            throw new RuntimeException("Stripe API key is not configured");
        }
    }
}