package com.zenfulcode.commercify.payment.domain.exception;

import com.zenfulcode.commercify.shared.domain.exception.EntityNotFoundException;
import lombok.Getter;

/**
 * Thrown when payment provider is not found or not supported
 */
@Getter
public class PaymentProviderNotFoundException extends EntityNotFoundException {
    private final String provider;

    public PaymentProviderNotFoundException(String provider) {
        super("PaymentProvider", provider);
        this.provider = provider;
    }
}