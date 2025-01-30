package com.zenfulcode.commercify.payment.infrastructure.gateway;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MobilepayTokenResponse(
        @JsonProperty("token_type")
        String tokenType,

        @JsonProperty("expires_in")
        String expiresIn,

        @JsonProperty("access_token")
        String accessToken,

        @JsonProperty("expires_on")
        String expiresOn,

        @JsonProperty("resource")
        String resource) {
}
