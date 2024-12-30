package com.zenfulcode.commercify.web.dto.response.auth;


import com.zenfulcode.commercify.web.dto.common.UserDTO;

public record RegisterUserResponse(UserDTO user, String message) {
    public static RegisterUserResponse RegistrationFailed(String message) {
        return new RegisterUserResponse(null, message);
    }

    public static RegisterUserResponse UserRegistered(UserDTO user) {
        return new RegisterUserResponse(user, "User registered successfully");
    }
}
