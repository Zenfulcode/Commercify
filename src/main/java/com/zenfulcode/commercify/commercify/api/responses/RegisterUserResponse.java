package com.zenfulcode.commercify.commercify.api.responses;


import com.zenfulcode.commercify.commercify.dto.UserDTO;

public record RegisterUserResponse(UserDTO user, String message) {
    public static RegisterUserResponse RegistrationFailed(String message) {
        return new RegisterUserResponse(null, message);
    }

    public static RegisterUserResponse UserRegistered(UserDTO user) {
        return new RegisterUserResponse(user, "User registered successfully");
    }
}
