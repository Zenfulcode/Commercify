package com.gostavdev.commercify.userservice.api.responses;

public record AuthResponse(String token, long expiresIn) {
}
