package com.zenfulcode.commercify.auth.domain.exception;

import com.zenfulcode.commercify.shared.domain.exception.DomainException;

public class InvalidAuthenticationException extends DomainException {
    public InvalidAuthenticationException(String message) {
        super(message);
    }
}
