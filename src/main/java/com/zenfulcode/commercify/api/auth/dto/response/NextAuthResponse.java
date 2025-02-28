package com.zenfulcode.commercify.api.auth.dto.response;

import com.zenfulcode.commercify.auth.application.service.AuthenticationResult;
import com.zenfulcode.commercify.auth.domain.model.AuthenticatedUser;

public record NextAuthResponse(
        String id,
        String name,
        String email,
        String accessToken,
        String refreshToken
) {
    public static NextAuthResponse from(AuthenticationResult result) {
        AuthenticatedUser user = result.user();
        return new NextAuthResponse(
                user.getUserId().toString(),
                user.getUsername(),
                user.getEmail(),
                result.accessToken(),
                result.refreshToken()
        );
    }

    public static NextAuthResponse fromUser(AuthenticatedUser user) {
        return new NextAuthResponse(
                user.getUserId().toString(),
                user.getUsername(),
                user.getEmail(),
                null,
                null
        );
    }
}