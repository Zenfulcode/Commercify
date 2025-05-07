package com.zenfulcode.commercify.api.auth.dto.response;

import com.zenfulcode.commercify.user.application.dto.response.UserProfileResponse;
import com.zenfulcode.commercify.auth.application.service.AuthenticationResult;
import com.zenfulcode.commercify.auth.domain.model.AuthenticatedUser;
import com.zenfulcode.commercify.auth.domain.model.UserRole;

import java.util.Set;

public record NextAuthResponse(
        UserProfileResponse user,
        String accessToken,
        String refreshToken,
        Set<UserRole> roles
) {
    public static NextAuthResponse from(AuthenticationResult result) {
        AuthenticatedUser user = result.user();
        return new NextAuthResponse(
                UserProfileResponse.fromUser(result.userInfo()),
                result.accessToken(),
                result.refreshToken(),
                user.getRoles()
        );
    }

    public static NextAuthResponse fromUser(AuthenticatedUser user) {
        return new NextAuthResponse(
                UserProfileResponse.fromAuthenticatedUser(user),
                null,
                null,
                user.getRoles()
        );
    }
}