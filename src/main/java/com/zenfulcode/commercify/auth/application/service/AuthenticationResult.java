package com.zenfulcode.commercify.auth.application.service;

import com.zenfulcode.commercify.auth.domain.model.AuthenticatedUser;

public record AuthenticationResult(
        String accessToken,
        String refreshToken,
        AuthenticatedUser user
) {
}