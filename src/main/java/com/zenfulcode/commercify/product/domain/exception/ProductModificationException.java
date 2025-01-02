package com.zenfulcode.commercify.product.domain.exception;

import com.zenfulcode.commercify.shared.domain.exception.DomainException;

public class ProductModificationException extends DomainException {
    public ProductModificationException(String message) {
        super(message);
    }
}
