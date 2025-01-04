package com.zenfulcode.commercify.auth.domain.service;

import com.zenfulcode.commercify.auth.domain.model.AuthenticatedUser;
import com.zenfulcode.commercify.user.domain.model.User;
import com.zenfulcode.commercify.user.domain.model.UserStatus;

public interface AuthenticationDomainService {
    AuthenticatedUser createAuthenticatedUser(User user);

    void validateUserStatus(UserStatus status);
}
