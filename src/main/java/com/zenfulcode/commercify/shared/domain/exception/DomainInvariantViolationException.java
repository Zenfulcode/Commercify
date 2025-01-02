package com.zenfulcode.commercify.shared.domain.exception;

import lombok.Getter;

@Getter
public class DomainInvariantViolationException extends DomainException {
    private final String invariantName;

    public DomainInvariantViolationException(String invariantName, String message) {
        super(message);
        this.invariantName = invariantName;
    }

}
