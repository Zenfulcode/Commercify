package com.zenfulcode.commercify.order.domain.exception;

import com.zenfulcode.commercify.shared.domain.exception.DomainForbiddenException;

public class UnauthorizedOrderFetchingException extends DomainForbiddenException {
    public UnauthorizedOrderFetchingException(String userId) {
        super("User is not authorized to fetch order for user ID: " + userId);
    }
}