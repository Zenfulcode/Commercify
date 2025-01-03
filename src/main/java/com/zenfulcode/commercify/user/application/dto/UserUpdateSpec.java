package com.zenfulcode.commercify.user.application.dto;

import com.zenfulcode.commercify.user.domain.model.UserRole;

import java.util.Set;

public record UserUpdateSpec(
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        Set<UserRole> roles
) {
    public boolean hasNameUpdate() {
        return firstName != null || lastName != null;
    }

    public boolean hasEmailUpdate() {
        return email != null;
    }

    public boolean hasPhoneUpdate() {
        return phoneNumber != null;
    }

    public boolean hasRolesUpdate() {
        return roles != null;
    }
}
