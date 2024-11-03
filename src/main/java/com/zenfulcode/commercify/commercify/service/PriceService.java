package com.zenfulcode.commercify.commercify.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Price;
import com.zenfulcode.commercify.commercify.api.requests.products.CreatePriceRequest;
import com.zenfulcode.commercify.commercify.api.requests.products.UpdatePriceRequest;
import com.zenfulcode.commercify.commercify.entity.PriceEntity;
import com.zenfulcode.commercify.commercify.entity.ProductEntity;
import com.zenfulcode.commercify.commercify.exception.StripeOperationException;
import com.zenfulcode.commercify.commercify.repository.PriceRepository;
import com.zenfulcode.commercify.commercify.service.stripe.StripeProductService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class PriceService {
    private final PriceRepository priceRepository;
    private final StripeProductService stripePriceService;

    public PriceEntity createPrice(CreatePriceRequest request, ProductEntity product) {
        PriceEntity price = PriceEntity.builder()
                .currency(request.currency())
                .amount(request.amount())
                .active(request.active())
                .product(product)
                .build();

        if (!Stripe.apiKey.isBlank() && product.getStripeId() != null) {
            String stripePriceId = stripePriceService.createStripePrice(
                    product.getStripeId(),
                    price
            );
            price.setStripePriceId(stripePriceId);
        }

        return priceRepository.save(price);
    }

    public void updatePrice(PriceEntity price, UpdatePriceRequest request) {
        if (request.currency() != null) price.setCurrency(request.currency());
        if (request.amount() != null) price.setAmount(request.amount());
        if (request.active() != null) price.setActive(request.active());

        if (!Stripe.apiKey.isBlank() && price.getStripePriceId() != null) {
            stripePriceService.updateStripePrice(price);
        }

        priceRepository.save(price);
    }

    public void deactivatePrice(PriceEntity price) throws StripeOperationException {
        price.setActive(false);
        if (!Stripe.apiKey.isBlank() && price.getStripePriceId() != null) {
            try {
                Price stripePrice = Price.retrieve(price.getStripePriceId());
                stripePriceService.deactivateStripePrices(stripePrice);
            } catch (StripeException e) {
                log.error("Failed to deactivate Stripe price: {}", e.getMessage());
                throw new StripeOperationException("Failed to deactivate Stripe price", e);
            }
        }
        priceRepository.save(price);
    }
}