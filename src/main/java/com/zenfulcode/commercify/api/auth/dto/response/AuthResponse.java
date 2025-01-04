package com.zenfulcode.commercify.api.auth.dto.response;

import com.zenfulcode.commercify.auth.application.service.AuthenticationResult;
import com.zenfulcode.commercify.auth.domain.model.UserRole;

import java.util.Set;
import java.util.stream.Collectors;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        String userId,
        String username,
        String email,
        Set<String> roles
) {
    public static AuthResponse from(AuthenticationResult result) {
        Set<String> roles = result.user().getRoles().stream()
                .map(UserRole::name)
                .collect(Collectors.toSet());

        return new AuthResponse(
                result.accessToken(),
                result.refreshToken(),
                "Bearer",
                result.user().getUserId(),
                result.user().getUsername(),
                result.user().getEmail(),
                roles
        );
    }
}