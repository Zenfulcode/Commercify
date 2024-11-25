package com.zenfulcode.commercify.commercify.api.requests;

import com.zenfulcode.commercify.commercify.dto.AddressDTO;

import java.util.UUID;

public record RegisterUserRequest(
        String email,
        String password,
        String firstName,
        String lastName,
        AddressDTO shippingAddress,
        AddressDTO billingAddress) {
    // Set a secure default password
    public RegisterUserRequest {
        if (password == null || password.isBlank()) {
            password = UUID.randomUUID().toString();
        }
    }
}