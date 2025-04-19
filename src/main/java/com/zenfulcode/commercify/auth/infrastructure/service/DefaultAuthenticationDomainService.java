package com.zenfulcode.commercify.auth.infrastructure.service;

import com.zenfulcode.commercify.auth.domain.model.AuthenticatedUser;
import com.zenfulcode.commercify.auth.domain.service.AuthenticationDomainService;
import com.zenfulcode.commercify.auth.infrastructure.mapper.UserRoleMapper;
import com.zenfulcode.commercify.shared.domain.exception.UserAccountLockedException;
import com.zenfulcode.commercify.user.domain.model.User;
import com.zenfulcode.commercify.user.domain.model.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DefaultAuthenticationDomainService implements AuthenticationDomainService {
    private final UserRoleMapper roleMapper;

    @Override
    public AuthenticatedUser createAuthenticatedUser(User user) {
        validateUserStatus(user.getStatus());

        return AuthenticatedUser.create(
                user.getId().toString(),
                user.getFullName(),
                user.getEmail(),
                user.getPassword(),
                roleMapper.mapRoles(user.getRoles()),
                user.getStatus() == UserStatus.ACTIVE,
                user.getStatus() != UserStatus.DEACTIVATED,
                user.getStatus() != UserStatus.SUSPENDED,
                true
        );
    }

    @Override
    public void validateUserStatus(UserStatus status) {
        if (status == UserStatus.SUSPENDED) {
            throw new UserAccountLockedException("Account is suspended");
        }
        if (status == UserStatus.DEACTIVATED) {
            throw new UserAccountLockedException("Account is deactivated");
        }
    }
}
