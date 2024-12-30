package com.zenfulcode.commercify.commercify.exception;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(Integer productId) {
        super("Product not found with ID: " + productId);
    }
}
