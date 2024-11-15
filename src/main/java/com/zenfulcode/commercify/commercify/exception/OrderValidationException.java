package com.zenfulcode.commercify.commercify.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class OrderValidationException extends RuntimeException {
    public OrderValidationException(String message) {
        super(message);
    }
}