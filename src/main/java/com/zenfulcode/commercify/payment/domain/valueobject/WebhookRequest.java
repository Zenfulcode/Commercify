package com.zenfulcode.commercify.payment.domain.valueobject;

import lombok.Builder;

import java.util.Map;

@Builder
public record WebhookRequest(
        String body,
        Map<String, String> headers
) {
}