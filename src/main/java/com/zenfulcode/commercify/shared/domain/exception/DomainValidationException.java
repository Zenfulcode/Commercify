package com.zenfulcode.commercify.shared.domain.exception;

import lombok.Getter;

import java.util.Collections;
import java.util.List;

@Getter
public abstract class DomainValidationException extends DomainException {
    private final List<String> violations;

    public DomainValidationException(String message, List<String> violations) {
        super(message);
        this.violations = Collections.unmodifiableList(violations);
    }

}
