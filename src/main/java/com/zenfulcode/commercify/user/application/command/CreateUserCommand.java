package com.zenfulcode.commercify.user.application.command;

import com.zenfulcode.commercify.user.domain.model.UserRole;

import java.util.Set;

public record CreateUserCommand(
        String email,
        String firstName,
        String lastName,
        String password,
        Set<UserRole> roles
) {}
