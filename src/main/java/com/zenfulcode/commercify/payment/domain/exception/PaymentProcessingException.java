package com.zenfulcode.commercify.payment.domain.exception;

import com.zenfulcode.commercify.shared.domain.exception.DomainException;
import lombok.Getter;

@Getter
public class PaymentProcessingException extends DomainException {
    private final String providerReference;

    public PaymentProcessingException(String message, Throwable cause) {
        this(message, null, cause);
    }

    public PaymentProcessingException(String message, String providerReference, Throwable cause) {
        super(message, cause);
        this.providerReference = providerReference;
    }
}
