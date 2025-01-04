package com.zenfulcode.commercify.user.domain.valueobject;

import com.zenfulcode.commercify.user.domain.model.UserRole;
import com.zenfulcode.commercify.user.domain.model.UserStatus;
import lombok.Builder;

import java.util.Set;

@Builder
public record UserSpecification(
        String firstName,
        String lastName,
        String email,
        String password,
        String phone,
        UserStatus status,
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
