package com.zenfulcode.commercify.payment.domain.exception;

import com.zenfulcode.commercify.shared.domain.exception.DomainValidationException;

import java.util.List;

public class WebhookValidationException extends DomainValidationException {

    public WebhookValidationException(String message, List<String> violations) {
        super(message, violations);
    }
}
