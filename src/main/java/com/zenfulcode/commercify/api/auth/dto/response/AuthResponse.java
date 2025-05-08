package com.zenfulcode.commercify.api.auth.dto.response;

import com.zenfulcode.commercify.auth.application.service.AuthenticationResult;
import com.zenfulcode.commercify.user.application.dto.response.UserProfileResponse;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        UserProfileResponse user
) {
    public static AuthResponse from(AuthenticationResult result) {
        return new AuthResponse(
                result.accessToken(),
                result.refreshToken(),
                UserProfileResponse.fromUser(result.userInfo())
        );
    }
}