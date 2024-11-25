package com.zenfulcode.commercify.commercify.api.requests;


import java.util.UUID;

public record RegisterUserRequest(
        String email,
        String password,
        String firstName,
        String lastName) {
    //    set a secure default password
    public RegisterUserRequest {
        if (password == null || password.isBlank()) {
            password = UUID.randomUUID().toString();
        }
    }
}