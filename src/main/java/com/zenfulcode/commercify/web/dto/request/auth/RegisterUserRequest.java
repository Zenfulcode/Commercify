package com.zenfulcode.commercify.web.dto.request.auth;

import com.zenfulcode.commercify.web.dto.common.AddressDTO;

import java.util.UUID;

public record RegisterUserRequest(
        String email,
        String password,
        String firstName,
        String lastName,
        Boolean isGuest,
        AddressDTO defaultAddress) {
    // Set a secure default password
    public RegisterUserRequest {
        if (password == null || password.isBlank()) {
            password = UUID.randomUUID().toString();
        }

        if (isGuest == null) {
            isGuest = false;
        }
    }
}