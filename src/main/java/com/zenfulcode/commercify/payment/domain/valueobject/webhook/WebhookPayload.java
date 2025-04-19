package com.zenfulcode.commercify.payment.domain.valueobject.webhook;

import java.time.Instant;

/**
 * Base interface for all webhook payloads
 */
public interface WebhookPayload {
    String getEventType();

    String getPaymentReference();

    Instant getTimestamp();

    boolean isValid();
}