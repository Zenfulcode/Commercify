package com.zenfulcode.commercify.payment.domain.exception;

import com.zenfulcode.commercify.shared.domain.exception.DomainValidationException;

import java.util.List;

/**
 * Thrown when payment validation fails
 */
public class PaymentValidationException extends DomainValidationException {
    public PaymentValidationException(String message, List<String> violations) {
        super(message, violations);
    }

    public PaymentValidationException(String message) {
        super(message, List.of(message));
    }
}