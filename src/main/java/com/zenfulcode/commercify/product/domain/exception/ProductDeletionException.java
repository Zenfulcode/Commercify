package com.zenfulcode.commercify.product.domain.exception;

import com.zenfulcode.commercify.shared.domain.exception.DomainValidationException;

import java.util.List;

public class ProductDeletionException extends DomainValidationException {
    public ProductDeletionException(String message, List<String> violations) {
        super(message, violations);
    }
}
