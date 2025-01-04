package com.zenfulcode.commercify.auth.domain.exception;

import com.zenfulcode.commercify.shared.domain.exception.DomainException;

public class InvalidCredentialsException extends DomainException {
    public InvalidCredentialsException() {
        super("Invalid username or password");
    }
}
