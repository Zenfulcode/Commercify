package com.zenfulcode.commercify.commercify.service.stripe;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Price;
import com.stripe.model.PriceSearchResult;
import com.stripe.param.PriceCreateParams;
import com.stripe.param.PriceSearchParams;
import com.stripe.param.PriceUpdateParams;
import com.zenfulcode.commercify.commercify.entity.PriceEntity;
import com.zenfulcode.commercify.commercify.exception.StripeOperationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class StripePriceService {

    public String createStripePrice(String productId, PriceEntity price) {
        if (Stripe.apiKey.isBlank()) {
            return null;
        }

        try {
            long amountInCents = (long) (price.getAmount() * 100);
            PriceCreateParams params = PriceCreateParams.builder()
                    .setProduct(productId)
                    .setCurrency(price.getCurrency().toLowerCase())
                    .setUnitAmount(amountInCents)
                    .setActive(price.getActive())
                    .build();

            Price stripePrice = Price.create(params);
            return stripePrice.getId();
        } catch (StripeException e) {
            log.error("Failed to create Stripe price: {}", e.getMessage());
            throw new StripeOperationException("Failed to create Stripe price", e);
        }
    }

    public void updateStripePrice(PriceEntity price) {
        try {
            Price stripePrice = Price.retrieve(price.getStripePriceId());

            if (price.getAmount() != null &&
                    stripePrice.getUnitAmount() != (long) (price.getAmount() * 100)) {
                // Deactivate old price and create new one
                deactivateStripePrice(stripePrice);
                createStripePrice(price.getProduct().getStripeId(), price);
            } else if (price.getActive() != null &&
                    stripePrice.getActive() != price.getActive()) {
                // Update active status
                PriceUpdateParams params = PriceUpdateParams.builder()
                        .setActive(price.getActive())
                        .build();
                stripePrice.update(params);
            }
        } catch (StripeException e) {
            log.error("Failed to update Stripe price: {}", e.getMessage());
            throw new StripeOperationException("Failed to update Stripe price", e);
        }
    }

    public void deactivateStripePrice(Price stripePrice) throws StripeException {
        PriceUpdateParams params = PriceUpdateParams.builder()
                .setActive(false)
                .build();
        stripePrice.update(params);
    }

    public Map<String, Price> getActiveStripePrices(String productId) {
        try {
            PriceSearchParams searchParams = PriceSearchParams.builder()
                    .setQuery("active:'true' product:'" + productId + "'")
                    .build();
            PriceSearchResult result = Price.search(searchParams);

            return result.getData().stream()
                    .collect(Collectors.toMap(
                            price -> price.getCurrency().toLowerCase(),
                            price -> price
                    ));
        } catch (StripeException e) {
            log.error("Failed to fetch Stripe prices: {}", e.getMessage());
            throw new StripeOperationException("Failed to fetch Stripe prices", e);
        }
    }
}