package com.zenfulcode.commercify.user.domain.exception;

import com.zenfulcode.commercify.shared.domain.exception.DomainValidationException;

import java.util.List;

public class UserValidationException extends DomainValidationException {
    public UserValidationException(List<String> violations) {
        super("User validation failed", violations);
    }
}