package com.zenfulcode.commercify.order.domain.exception;

import com.zenfulcode.commercify.shared.domain.exception.DomainForbiddenException;
import com.zenfulcode.commercify.user.domain.valueobject.UserId;

public class UnauthorizedOrderCreationException extends DomainForbiddenException {
    public UnauthorizedOrderCreationException(UserId userId) {
        super("User is not authorized to create order for user ID: " + userId);
    }
}