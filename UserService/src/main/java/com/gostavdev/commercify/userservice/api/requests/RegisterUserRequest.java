package com.gostavdev.commercify.userservice.api.requests;

public record RegisterUserRequest(
        String email,
        String password,
        String firstName,
        String lastName,
        boolean admin) {
}
