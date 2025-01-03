package com.zenfulcode.commercify.order.domain.exception;

import com.zenfulcode.commercify.shared.domain.exception.DomainValidationException;

import java.util.List;

public class OrderValidationException extends DomainValidationException {
    public OrderValidationException(String message) {
        super(message, List.of(message));
    }

    public OrderValidationException(List<String> violations) {
        super("Order validation failed: " + String.join(", ", violations), violations);
    }
}
