package com.zenfulcode.commercify.user.domain.service;

import com.zenfulcode.commercify.shared.domain.event.DomainEventPublisher;
import com.zenfulcode.commercify.user.application.dto.UserUpdateSpec;
import com.zenfulcode.commercify.user.domain.model.User;
import com.zenfulcode.commercify.user.domain.model.UserRole;
import com.zenfulcode.commercify.user.domain.model.UserStatus;
import com.zenfulcode.commercify.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserDomainService {
    private final UserRepository userRepository;
    private final DomainEventPublisher domainEventPublisher;

    /**
     * Creates a new user with domain logic and validation
     */
    public User createUser(
            String email,
            String firstName,
            String lastName,
            String hashedPassword,
            Set<UserRole> roles
    ) {
        // Validate that email is unique
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists: " + email);
        }

        return User.create(
                email,
                firstName,
                lastName,
                hashedPassword,
                roles
        );
    }

    /**
     * Updates user details with domain validation
     */
    public void updateUser(User user, UserUpdateSpec updateSpec) {
        // Validate email uniqueness if email is being changed
        if (updateSpec.email() != null && !updateSpec.email().equals(user.getEmail())) {
            if (userRepository.existsByEmail(updateSpec.email())) {
                throw new IllegalArgumentException("Email already exists: " + updateSpec.email());
            }
        }

        // Update user details
        user.update(updateSpec);
    }

    /**
     * Updates user status with domain logic
     */
    public void updateUserStatus(User user, UserStatus newStatus) {
        // Update user status
        user.updateStatus(newStatus);
    }

    /**
     * Updates user password with domain logic
     */
    public void updatePassword(User user, String newHashedPassword) {
        // Validate password complexity (example validation)
        validatePasswordComplexity(newHashedPassword);

        // Update password
        user.updatePassword(newHashedPassword);
    }

    /**
     * Validate password complexity (example implementation)
     */
    private void validatePasswordComplexity(String hashedPassword) {
        // Example basic validation - can be expanded
        if (hashedPassword == null || hashedPassword.length() < 8) {
            throw new IllegalArgumentException("Password is too short");
        }
    }
}