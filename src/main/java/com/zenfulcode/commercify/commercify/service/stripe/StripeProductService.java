package com.zenfulcode.commercify.commercify.service.stripe;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Product;
import com.stripe.param.ProductCreateParams;
import com.stripe.param.ProductUpdateParams;
import com.zenfulcode.commercify.commercify.entity.ProductEntity;
import com.zenfulcode.commercify.commercify.exception.StripeOperationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class StripeProductService {

    public String createStripeProduct(ProductEntity product) {
        if (Stripe.apiKey.isBlank()) {
            return null;
        }

        try {
            ProductCreateParams params = ProductCreateParams.builder()
                    .setName(product.getName())
                    .setDescription(product.getDescription())
                    .build();
            Product stripeProduct = Product.create(params);
            return stripeProduct.getId();
        } catch (StripeException e) {
            log.error("Failed to create Stripe product: {}", e.getMessage());
            throw new StripeOperationException("Failed to create Stripe product", e);
        }
    }

    public void updateStripeProduct(String stripeId, ProductEntity product) {
        try {
            Product stripeProduct = Product.retrieve(stripeId);
            ProductUpdateParams params = ProductUpdateParams.builder()
                    .setName(product.getName())
                    .setDescription(product.getDescription())
                    .setActive(product.getActive())
                    .build();
            stripeProduct.update(params);
        } catch (StripeException e) {
            log.error("Failed to update Stripe product: {}", e.getMessage());
            throw new StripeOperationException("Failed to update Stripe product", e);
        }
    }

    public void deleteStripeProduct(String stripeId) {
        try {
            Product stripeProduct = Product.retrieve(stripeId);
            stripeProduct.delete();
        } catch (StripeException e) {
            log.error("Failed to delete Stripe product: {}", e.getMessage());
            throw new StripeOperationException("Failed to delete Stripe product", e);
        }
    }
}
