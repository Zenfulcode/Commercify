package com.zenfulcode.commercify.auth.application.service;

import com.zenfulcode.commercify.auth.domain.model.AuthenticatedUser;
import com.zenfulcode.commercify.user.domain.model.User;

public record AuthenticationResult(
        String accessToken,
        String refreshToken,
        AuthenticatedUser user,
        User userInfo
) {
}