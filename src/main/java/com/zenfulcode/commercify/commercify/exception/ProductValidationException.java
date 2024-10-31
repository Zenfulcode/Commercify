package com.zenfulcode.commercify.commercify.exception;

import java.util.List;

public class ProductValidationException extends RuntimeException {
    private final List<String> errors;

    public ProductValidationException(List<String> errors) {
        super("Product validation failed: " + String.join(", ", errors));
        this.errors = errors;
    }

    public List<String> getErrors() {
        return errors;
    }
}
