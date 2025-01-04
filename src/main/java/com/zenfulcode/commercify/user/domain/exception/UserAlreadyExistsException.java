package com.zenfulcode.commercify.user.domain.exception;

import com.zenfulcode.commercify.shared.domain.exception.DomainException;

public class UserAlreadyExistsException extends DomainException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
