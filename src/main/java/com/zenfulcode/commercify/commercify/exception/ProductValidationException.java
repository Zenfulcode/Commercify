package com.zenfulcode.commercify.commercify.exception;

import lombok.Getter;

import java.util.List;

@Getter
public class ProductValidationException extends RuntimeException {
    private final List<String> errors;

    public ProductValidationException(List<String> errors) {
        super("Product validation failed: " + String.join(", ", errors));
        this.errors = errors;
    }

}
