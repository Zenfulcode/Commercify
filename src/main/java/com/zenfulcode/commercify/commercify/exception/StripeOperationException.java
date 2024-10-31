package com.zenfulcode.commercify.commercify.exception;

public class StripeOperationException extends RuntimeException {
    public StripeOperationException(String message) {
        super(message);
    }

    public StripeOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
