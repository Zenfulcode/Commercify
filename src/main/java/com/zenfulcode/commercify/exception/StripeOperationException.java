package com.zenfulcode.commercify.exception;

public class StripeOperationException extends RuntimeException {

    public StripeOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
