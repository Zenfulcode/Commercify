package com.zenfulcode.commercify.user.domain.exception;

import com.zenfulcode.commercify.shared.domain.exception.DomainException;
import com.zenfulcode.commercify.user.domain.valueobject.UserId;
import lombok.Getter;

@Getter
public class UserNotFoundException extends DomainException {
    private final Object identifier;

    public UserNotFoundException(UserId userId) {
        super("User not found with ID: " + userId);
        this.identifier = userId;
    }

    public UserNotFoundException(String email) {
        super("User not found with Email: " + email);
        this.identifier = email;
    }

    public UserNotFoundException(String message, String identifier) {
        super(message + ": " + identifier);
        this.identifier = identifier;
    }
}