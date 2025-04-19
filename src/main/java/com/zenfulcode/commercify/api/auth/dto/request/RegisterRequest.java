package com.zenfulcode.commercify.api.auth.dto.request;

public record RegisterRequest(
        String firstName,
        String lastName,
        String email,
        String password,
        String phone
) {
}
