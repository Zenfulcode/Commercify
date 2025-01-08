package com.zenfulcode.commercify.commercify.api.requests;

import lombok.Builder;

@Builder
public record WebhookPayload(
        String msn,
        String reference,
        String name,
        boolean success
) {
}
