package com.zenfulcode.commercify.payment.domain.valueobject;

public record MobilepayWebhookResponse(
        String secret,
        String id
) {
}
