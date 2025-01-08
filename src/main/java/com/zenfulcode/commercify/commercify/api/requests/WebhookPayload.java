package com.zenfulcode.commercify.commercify.api.requests;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record WebhookPayload(
        @JsonProperty("msn") String msn,
        @JsonProperty("reference") String reference,
        @JsonProperty("pspReference") String pspReference,
        @JsonProperty("name") String name,
        @JsonProperty("amount") Object amount,
        @JsonProperty("timestamp") String timestamp,
        @JsonProperty("idempotencyKey") String idempotencyKey,
        @JsonProperty("success") boolean success
) {
}