package com.zenfulcode.commercify.commercify.api.requests;


public record RegisterUserRequest(
        String email,
        String password,
        String firstName,
        String lastName) {
}