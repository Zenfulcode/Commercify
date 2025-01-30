package com.zenfulcode.commercify.payment.domain.exception;

import com.zenfulcode.commercify.shared.domain.exception.DomainException;

public class WebhookProcessingException extends DomainException {
    public WebhookProcessingException(String message) {
        super("Webhook validation mismatch: " + message);
    }
}
