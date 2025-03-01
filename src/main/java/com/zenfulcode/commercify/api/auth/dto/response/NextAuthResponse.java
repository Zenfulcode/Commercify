package com.zenfulcode.commercify.api.auth.dto.response;

import com.zenfulcode.commercify.auth.application.service.AuthenticationResult;
import com.zenfulcode.commercify.auth.domain.model.AuthenticatedUser;
import com.zenfulcode.commercify.auth.domain.model.UserRole;

import java.util.Set;

public record NextAuthResponse(
        String id,
        String name,
        String email,
        String accessToken,
        String refreshToken,
        Set<UserRole> roles
) {
    public static NextAuthResponse from(AuthenticationResult result) {
        AuthenticatedUser user = result.user();
        return new NextAuthResponse(
                user.getUserId().toString(),
                user.getUsername(),
                user.getEmail(),
                result.accessToken(),
                result.refreshToken(),
                user.getRoles()
        );
    }

    public static NextAuthResponse fromUser(AuthenticatedUser user) {
        return new NextAuthResponse(
                user.getUserId().toString(),
                user.getUsername(),
                user.getEmail(),
                null,
                null,
                user.getRoles()
        );
    }
}