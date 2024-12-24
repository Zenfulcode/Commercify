package com.zenfulcode.commercify.commercify.integration;

public record WebhookResponse(
        String secret,
        String id
) {
}