package com.zenfulcode.commercify.user.domain.valueobject;

import com.zenfulcode.commercify.user.domain.model.UserRole;

import java.util.Set;

public record UserSpecification(
        String firstName,
        String lastName,
        String email,
        String password,
        String phone,
        Set<UserRole> roles
) {
    public boolean hasBasicInfoUpdate() {
        return firstName != null || lastName != null;
    }

    public boolean hasContactInfoUpdate() {
        return email != null || phone != null;
    }

    public boolean hasCredentialsUpdate() {
        return password != null;
    }

    public boolean hasRoleUpdate() {
        return roles != null;
    }
}
