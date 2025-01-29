package com.zenfulcode.commercify.payment.domain.valueobject;

public record MobilepayWebhookRegistrationResponse(
        String secret,
        String id
) {
}
