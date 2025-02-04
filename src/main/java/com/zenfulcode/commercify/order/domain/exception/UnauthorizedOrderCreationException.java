package com.zenfulcode.commercify.order.domain.exception;

import com.zenfulcode.commercify.shared.domain.exception.DomainForbiddenException;

public class UnauthorizedOrderCreationException extends DomainForbiddenException {
    public UnauthorizedOrderCreationException(String userId) {
        super("User is not authorized to create order for user ID: " + userId);
    }
}