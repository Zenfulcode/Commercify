package com.zenfulcode.commercify.commercify.api.responses;


import com.zenfulcode.commercify.commercify.dto.UserDTO;

public record AuthResponse(UserDTO user, String token, long expiresIn, String message) {
    public static AuthResponse UserAuthenticated(UserDTO user, String token, long expiresIn) {
        return new AuthResponse(user, token, expiresIn, "User authenticated");
    }

    public static AuthResponse AuthenticationFailed(String message) {
        return new AuthResponse(null, null, 0, message);
    }
}
