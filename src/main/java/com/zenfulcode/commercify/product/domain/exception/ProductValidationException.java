package com.zenfulcode.commercify.product.domain.exception;

import com.zenfulcode.commercify.shared.domain.exception.DomainValidationException;

import java.util.List;

public class ProductValidationException extends DomainValidationException {
    public ProductValidationException(List<String> violations) {
        super("Product validation failed", violations);
    }
}
