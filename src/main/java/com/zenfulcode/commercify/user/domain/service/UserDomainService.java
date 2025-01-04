package com.zenfulcode.commercify.user.domain.service;

import com.zenfulcode.commercify.user.application.dto.UserUpdateSpec;
import com.zenfulcode.commercify.user.domain.exception.InvalidUserStateException;
import com.zenfulcode.commercify.user.domain.exception.UserValidationException;
import com.zenfulcode.commercify.user.domain.model.User;
import com.zenfulcode.commercify.user.domain.model.UserRole;
import com.zenfulcode.commercify.user.domain.model.UserStatus;
import com.zenfulcode.commercify.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class UserDomainService {
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@(.+)$"
    );
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^\\+?[1-9]\\d{1,14}$"
    );

    private final UserRepository userRepository;
    private final UserValidationPolicy validationPolicy;

    /**
     * Creates a new user with validation
     */
    public User createUser(
            String email,
            String firstName,
            String lastName,
            String hashedPassword,
            Set<UserRole> roles
    ) {
        validateNewUser(email, firstName, lastName, hashedPassword);
        validateRoles(roles);

        return User.create(
                email,
                firstName,
                lastName,
                hashedPassword,
                roles
        );
    }

    /**
     * Updates user information
     */
    public void updateUser(User user, UserUpdateSpec updateSpec) {
        List<String> violations = new ArrayList<>();

        // Validate and apply name updates
        if (updateSpec.hasNameUpdate()) {
            if (updateSpec.firstName() != null) {
                validateFirstName(updateSpec.firstName(), violations);
            }
            if (updateSpec.lastName() != null) {
                validateLastName(updateSpec.lastName(), violations);
            }
        }

        // Validate and apply email update
        if (updateSpec.hasEmailUpdate()) {
            validateEmail(updateSpec.email(), violations);
            validateEmailUniqueness(updateSpec.email(), user);
        }

        // Validate and apply phone update
        if (updateSpec.hasPhoneUpdate() && updateSpec.phoneNumber() != null) {
            validatePhoneNumber(updateSpec.phoneNumber(), violations);
        }

        // Validate and apply role updates
        if (updateSpec.hasRolesUpdate()) {
            validateRoles(updateSpec.roles());
        }

        if (!violations.isEmpty()) {
            throw new UserValidationException(violations);
        }

        // Apply updates
        user.updateProfile(
                updateSpec.firstName(),
                updateSpec.lastName(),
                updateSpec.phoneNumber()
        );

        if (updateSpec.hasEmailUpdate()) {
            user.updateEmail(updateSpec.email());
        }

        if (updateSpec.hasRolesUpdate()) {
            user.updateRoles(updateSpec.roles());
        }
    }

    /**
     * Updates user's password
     */
    public void updatePassword(User user, String hashedPassword) {
        validatePassword(hashedPassword);
        user.updatePassword(hashedPassword);
    }

    /**
     * Updates user's status with validation
     */
    public void updateUserStatus(User user, UserStatus newStatus) {
        validateStatusTransition(user, newStatus);
        user.updateStatus(newStatus);
    }

    private void validateNewUser(String email, String firstName, String lastName, String password) {
        List<String> violations = new ArrayList<>();

        validateEmail(email, violations);
        validateFirstName(firstName, violations);
        validateLastName(lastName, violations);
        validatePassword(password);
        validateEmailUniqueness(email, null);

        if (!violations.isEmpty()) {
            throw new UserValidationException(violations);
        }
    }

    private void validateEmail(String email, List<String> violations) {
        if (email == null || email.isBlank()) {
            violations.add("Email is required");
            return;
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            violations.add("Invalid email format");
        }
    }

    private void validateEmailUniqueness(String email, User existingUser) {
        if (userRepository.findByEmail(email)
                .map(user -> !user.equals(existingUser))
                .orElse(false)) {
            throw new UserValidationException(List.of("Email already exists"));
        }
    }

    private void validateFirstName(String firstName, List<String> violations) {
        if (firstName == null || firstName.isBlank()) {
            violations.add("First name is required");
        } else if (firstName.length() < 2 || firstName.length() > 50) {
            violations.add("First name must be between 2 and 50 characters");
        }
    }

    private void validateLastName(String lastName, List<String> violations) {
        if (lastName == null || lastName.isBlank()) {
            violations.add("Last name is required");
        } else if (lastName.length() < 2 || lastName.length() > 50) {
            violations.add("Last name must be between 2 and 50 characters");
        }
    }

    private void validatePhoneNumber(String phoneNumber, List<String> violations) {
        if (phoneNumber != null && !phoneNumber.isBlank() &&
                !PHONE_PATTERN.matcher(phoneNumber).matches()) {
            violations.add("Invalid phone number format");
        }
    }

    private void validatePassword(String hashedPassword) {
        if (hashedPassword == null || hashedPassword.isBlank()) {
            throw new UserValidationException(List.of("Password is required"));
        }
    }

    private void validateRoles(Set<UserRole> roles) {
        if (roles == null || roles.isEmpty()) {
            throw new UserValidationException(List.of("User must have at least one role"));
        }

        // Additional role validation logic can be added here
        // For example, ensuring admin role assignments follow specific rules
    }

    private void validateStatusTransition(User user, UserStatus newStatus) {
        // Additional status transition validation logic can be added here
        // For example, checking if user has pending orders before deactivation
        if (validationPolicy.canTransitionToStatus(user, newStatus)) {
            throw new InvalidUserStateException(
                    user.getId(),
                    user.getStatus(),
                    newStatus,
                    "Status transition not allowed by policy"
            );
        }
    }
}