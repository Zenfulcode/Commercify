package com.zenfulcode.commercify.shared.domain.exception;

public class UserAccountLockedException extends DomainException {
    public UserAccountLockedException(String message) {
        super(message);
    }
}