package com.zenfulcode.commercify.product.domain.exception;

import com.zenfulcode.commercify.shared.domain.exception.DomainException;

public class InvalidPriceException extends DomainException {
    public InvalidPriceException(String message) {
        super(message);
    }

    public InvalidPriceException(String message, Throwable cause) {
        super(message, cause);
    }
}
