package com.zenfulcode.commercify.commercify.integration;

public record WebhookRegistrationResponse(
        String secret,
        String id
) {
}