package com.zenfulcode.commercify.user.domain.service;

import com.zenfulcode.commercify.shared.domain.event.DomainEventPublisher;
import com.zenfulcode.commercify.user.domain.event.UserCreatedEvent;
import com.zenfulcode.commercify.user.domain.event.UserStatusChangedEvent;
import com.zenfulcode.commercify.user.domain.exception.UserAlreadyExistsException;
import com.zenfulcode.commercify.user.domain.model.User;
import com.zenfulcode.commercify.user.domain.model.UserStatus;
import com.zenfulcode.commercify.user.domain.repository.UserRepository;
import com.zenfulcode.commercify.user.domain.valueobject.UserDeletionValidation;
import com.zenfulcode.commercify.user.domain.valueobject.UserSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDomainService {
    private final UserStateFlow userStateFlow;
    private final UserValidationService validationService;
    private final DomainEventPublisher eventPublisher;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    /**
     * Creates a new user with validation and enrichment
     */
    public User createUser(UserSpecification spec) {
        // Check if username or email already exists
        if (userRepository.existsByEmail(spec.email())) {
            throw new UserAlreadyExistsException("Email already exists");
        }

        // Create user with encrypted password
        User user = User.create(
                spec.firstName(),
                spec.lastName(),
                spec.email(),
                passwordEncoder.encode(spec.password()),
                spec.roles(),
                spec.phone()
        );

        // Validate user
        validationService.validateCreateUser(user);

        // Register creation event
        user.registerEvent(new UserCreatedEvent(
                user.getId(),
                user.getEmail(),
                user.getStatus()
        ));

        return user;
    }

    /**
     * Updates user status with validation
     */
    public void updateUserStatus(User user, UserStatus newStatus) {
        // Validate status transition
        validationService.validateStatusTransition(user, newStatus);

        // If transitioning to deactivated state, validate deactivation
        if (newStatus == UserStatus.DEACTIVATED) {
            validationService.validateDeactivation(user);
        }

        UserStatus oldStatus = user.getStatus();
        user.updateStatus(newStatus);

        // Register status change event
        user.registerEvent(new UserStatusChangedEvent(
                user.getId(),
                oldStatus,
                newStatus
        ));
    }

    /**
     * Updates user information with validation
     */
    public void updateUserInfo(User user, UserSpecification updateSpec) {
        // Update basic information if provided
        if (updateSpec.hasBasicInfoUpdate()) {
            user.updateProfile(updateSpec.firstName(), updateSpec.lastName());
        }

        if (updateSpec.hasContactInfoUpdate()) {
            user.updateEmail(updateSpec.email());
        }

        if (updateSpec.phone() != null) {
            user.updatePhone(updateSpec.phone());
        }

        // Validate updated user
        validationService.validateUpdateUser(user);
    }

    /**
     * Changes user password with validation
     */
    public void changePassword(User user, String newPassword) {
        // Validate new password
        validationService.validatePasswordChange(newPassword);

        // Update password
        user.updatePassword(passwordEncoder.encode(newPassword));
    }

    /**
     * Validates if a user can be deleted
     */
    public UserDeletionValidation validateUserDeletion(User user) {
        List<String> issues = new ArrayList<>();

        try {
            validationService.validateAccountDeletion(user);
        } catch (Exception e) {
            issues.add(e.getMessage());
        }

        return new UserDeletionValidation(issues.isEmpty(), issues);
    }

    /**
     * Deactivates a user account with validation
     */
    public void deactivateUser(User user) {
        validationService.validateDeactivation(user);
        updateUserStatus(user, UserStatus.DEACTIVATED);
    }

    /**
     * Suspends a user account with validation
     */
    public void suspendUser(User user) {
        updateUserStatus(user, UserStatus.SUSPENDED);
    }

    /**
     * Reactivates a suspended user account with validation
     */
    public void reactivateUser(User user) {
        if (user.getStatus() != UserStatus.SUSPENDED) {
            throw new IllegalStateException("Can only reactivate suspended users");
        }
        updateUserStatus(user, UserStatus.ACTIVE);
    }
}