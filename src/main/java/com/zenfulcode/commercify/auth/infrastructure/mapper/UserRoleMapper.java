package com.zenfulcode.commercify.auth.infrastructure.mapper;

import com.zenfulcode.commercify.auth.domain.model.UserRole;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserRoleMapper {
    public Set<UserRole> mapRoles(Collection<com.zenfulcode.commercify.user.domain.model.UserRole> roles) {
        return roles.stream()
                .map(role -> UserRole.valueOf("ROLE_" + role.name()))
                .collect(Collectors.toSet());
    }
}
