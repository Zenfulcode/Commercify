package com.zenfulcode.commercify.user.application.dto.response;

import com.zenfulcode.commercify.auth.domain.model.AuthenticatedUser;
import com.zenfulcode.commercify.user.domain.model.User;
import com.zenfulcode.commercify.user.domain.model.UserStatus;

import java.util.List;

public record UserProfileResponse(
        String id,
        String email,
        String firstName,
        String lastName,
        String phoneNumber,
        String status,
        List<String> roles
) {
    public static UserProfileResponse fromUser(User user) {
        return new UserProfileResponse(
                user.getId().toString(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhoneNumber(),
                user.getStatus().name(),
                user.getRoles().stream()
                        .map(Enum::name)
                        .toList()
        );
    }

    public static UserProfileResponse fromAuthenticatedUser(AuthenticatedUser user) {
        return new UserProfileResponse(
                user.getUserId().getId(),
                user.getEmail(),
                user.getUsername(),
                "",
                "",
                UserStatus.ACTIVE.toString(),
                user.getRoles().stream()
                        .map(Enum::name)
                        .toList()
        );
    }
}
