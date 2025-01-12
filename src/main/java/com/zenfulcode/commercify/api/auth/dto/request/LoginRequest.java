package com.zenfulcode.commercify.api.auth.dto.request;

public record LoginRequest(
        String email,
        String password
) {
}