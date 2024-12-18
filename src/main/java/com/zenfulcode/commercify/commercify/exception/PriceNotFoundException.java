package com.zenfulcode.commercify.commercify.exception;

public class PriceNotFoundException extends RuntimeException {
    public PriceNotFoundException(Long priceId) {
        super("Price not found with ID: " + priceId);
    }
}