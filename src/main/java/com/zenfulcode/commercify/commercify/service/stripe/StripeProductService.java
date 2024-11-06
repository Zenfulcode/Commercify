package com.zenfulcode.commercify.commercify.service.stripe;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Price;
import com.stripe.model.Product;
import com.stripe.param.*;
import com.zenfulcode.commercify.commercify.api.requests.products.CreatePriceRequest;
import com.zenfulcode.commercify.commercify.api.requests.products.UpdatePriceRequest;
import com.zenfulcode.commercify.commercify.entity.ProductEntity;
import com.zenfulcode.commercify.commercify.exception.StripeOperationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class StripeProductService {
    public String createStripeProduct(ProductEntity product) {
        if (Stripe.apiKey == null || Stripe.apiKey.isBlank())
            throw new RuntimeException("Can't CREATE product in stripe without stripe key");

        try {
            ProductCreateParams params = ProductCreateParams.builder()
                    .setName(product.getName())
                    .setDescription(product.getDescription())
                    .build();
            Product stripeProduct = Product.create(params);
            return stripeProduct.getId();
        } catch (StripeException e) {
            throw new StripeOperationException("Failed to create Stripe product", e);
        }
    }

    public void updateStripeProduct(String stripeId, ProductEntity product) {
        if (Stripe.apiKey == null || Stripe.apiKey.isBlank())
            throw new RuntimeException("Can't UPDATE product in stripe without stripe key");

        try {
            Product stripeProduct = Product.retrieve(stripeId);
            ProductUpdateParams params = ProductUpdateParams.builder()
                    .setName(product.getName())
                    .setDescription(product.getDescription())
                    .setActive(product.getActive())
                    .build();
            stripeProduct.update(params);
        } catch (StripeException e) {
            throw new StripeOperationException("Failed to update Stripe product", e);
        }
    }

    public void createStripePrice(ProductEntity product, CreatePriceRequest request) {
        if (Stripe.apiKey == null || product.getStripeId() == null || Stripe.apiKey.isBlank())
            throw new RuntimeException("Can't CREATE price from stripe without stripe key");

        try {
            long amountInCents = (long) (request.amount() * 100);
            PriceCreateParams params = PriceCreateParams.builder()
                    .setProduct(product.getStripeId())
                    .setCurrency(request.currency().toLowerCase())
                    .setUnitAmount(amountInCents)
                    .setActive(product.getActive())
                    .build();

            Price stripePrice = Price.create(params);
            product.setStripePriceId(stripePrice.getId());
        } catch (StripeException e) {
            log.error("Failed to create Stripe price: {}", e.getMessage());
            throw new StripeOperationException("Failed to create Stripe price", e);
        }
    }

    public void updateStripePrice(ProductEntity product, UpdatePriceRequest request) {
        if (Stripe.apiKey == null || product.getStripeId() == null || Stripe.apiKey.isBlank())
            throw new RuntimeException("Can't UPDATE price from stripe without stripe key");

        if (product.getUnitPrice().equals(request.amount()) && product.getCurrency().equals(request.currency())) {
            return;
        }

        try {
            // First, deactivate the old price
            Price oldPrice = Price.retrieve(product.getStripePriceId());
            PriceUpdateParams deactivateParams = PriceUpdateParams.builder()
                    .setActive(false)
                    .build();
            oldPrice.update(deactivateParams);

            // Create a new price
            long amountInCents = (long) (request.amount() * 100);
            PriceCreateParams params = PriceCreateParams.builder()
                    .setProduct(product.getStripeId())
                    .setCurrency(request.currency().toLowerCase())
                    .setUnitAmount(amountInCents)
                    .setActive(true)
                    .build();

            Price newPrice = Price.create(params);

            // Update the product entity with the new price ID
            product.setStripePriceId(newPrice.getId());
            product.setUnitPrice(request.amount());
            product.setCurrency(request.currency());

        } catch (StripeException e) {
            log.error("Failed to update Stripe price: {}", e.getMessage());
            throw new StripeOperationException("Failed to update Stripe price", e);
        }
    }

    public void deactivateProduct(ProductEntity productEnt) {
        if (Stripe.apiKey == null || productEnt.getStripeId() == null || Stripe.apiKey.isBlank())
            throw new RuntimeException("Can't DELETE product from stripe without stripe key");

        try {
            // Deactivate all associated prices
            deactivateStripePrices(productEnt.getStripeId());
        } catch (StripeException e) {
            log.error("Failed to deactivate Stripe prices: {}", e.getMessage());
            throw new RuntimeException("Failed to deactivate Stripe prices: " + e.getMessage(), e);
        }

        try {
            // Deactivate the product
            Product stripeProduct = Product.retrieve(productEnt.getStripeId());
            ProductUpdateParams params = ProductUpdateParams.builder()
                    .setActive(false)
                    .build();

            stripeProduct.update(params);
        } catch (StripeException e) {
            log.error("Failed to deactivate product: {}", e.getMessage());
            throw new RuntimeException("Failed to deactivate product: " + e.getMessage(), e);
        }

        productEnt.setActive(false);
    }

    public void reactivateProduct(ProductEntity productEnt) {
        try {
            // Reactivate the product in Stripe
            Product stripeProduct = Product.retrieve(productEnt.getStripeId());
            stripeProduct.update(Map.of("active", true));

            // Reactivate associated prices
            activateStripePrices(productEnt.getStripeId());
        } catch (StripeException e) {
            throw new RuntimeException("Stripe error: " + e.getMessage(), e);
        }
    }

    private void deactivateStripePrices(String stripeProductId) throws StripeException {
        PriceSearchParams params = PriceSearchParams.builder()
                .setQuery("product:'" + stripeProductId + "' AND active:'true'")
                .build();

        Price.search(params).getData().forEach(price -> {
            try {
                Price retrievedPrice = Price.retrieve(price.getId());
                retrievedPrice.update(Map.of("active", false));
            } catch (StripeException e) {
                throw new RuntimeException("Failed to deactivate price: " + price.getId(), e);
            }
        });
    }

    public void activateStripePrices(String stripeProductId) throws StripeException {
        PriceSearchParams params = PriceSearchParams.builder()
                .setQuery("product:'" + stripeProductId + "' AND active:'false'")
                .build();

        Price.search(params).getData().forEach(price -> {
            try {
                Price retrievedPrice = Price.retrieve(price.getId());
                retrievedPrice.update(Map.of("active", true));
            } catch (StripeException e) {
                throw new RuntimeException("Failed to activate price: " + price.getId(), e);
            }
        });
    }
}
