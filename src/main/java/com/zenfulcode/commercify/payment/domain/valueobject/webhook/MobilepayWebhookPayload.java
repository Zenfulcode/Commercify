package com.zenfulcode.commercify.payment.domain.valueobject.webhook;

import java.time.Instant;

public record MobilepayWebhookPayload(
        String msn,
        String reference,
        String pspReference,
        String name,
        MobilepayAmount amount,
        Instant timestamp,
        String idempotencyKey,
        boolean success
) implements WebhookPayload {

    @Override
    public String getEventType() {
        return name;
    }

    @Override
    public String getPaymentReference() {
        return reference;
    }

    @Override
    public Instant getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean isValid() {
        return reference != null && name != null;
    }

    public record MobilepayAmount(
            String currency,
            long value
    ) {
    }
}
