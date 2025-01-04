package com.zenfulcode.commercify.user.domain.service;

import com.zenfulcode.commercify.user.domain.exception.InvalidUserStateTransitionException;
import com.zenfulcode.commercify.user.domain.exception.UserValidationException;
import com.zenfulcode.commercify.user.domain.model.User;
import com.zenfulcode.commercify.user.domain.model.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class UserValidationService {
    private final UserStateFlow userStateFlow;
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@(.+)$"
    );
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$"
    );
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^\\+?[1-9]\\d{1,14}$"
    );

    public void validateCreateUser(User user) {
        List<String> violations = new ArrayList<>();

        validateBasicUserInfo(user, violations);
        validateContactInfo(user, violations);
        validatePassword(user.getPassword(), violations);

        if (!violations.isEmpty()) {
            throw new UserValidationException(violations);
        }
    }

    public void validateUpdateUser(User user) {
        List<String> violations = new ArrayList<>();

        validateBasicUserInfo(user, violations);
        validateContactInfo(user, violations);

        if (!violations.isEmpty()) {
            throw new UserValidationException(violations);
        }
    }

    public void validateStatusTransition(User user, UserStatus newStatus) {
        if (!userStateFlow.canTransition(user.getStatus(), newStatus)) {
            throw new InvalidUserStateTransitionException(
                    user.getId(),
                    user.getStatus(),
                    newStatus,
                    "Invalid status transition"
            );
        }
    }

    public void validatePasswordChange(String newPassword) {
        List<String> violations = new ArrayList<>();
        validatePassword(newPassword, violations);

        if (!violations.isEmpty()) {
            throw new UserValidationException(violations);
        }
    }

    private void validateBasicUserInfo(User user, List<String> violations) {
        if (user.getFirstName() == null || user.getFirstName().isBlank()) {
            violations.add("First name is required");
        }
        if (user.getLastName() == null || user.getLastName().isBlank()) {
            violations.add("Last name is required");
        }
    }

    private void validateContactInfo(User user, List<String> violations) {
        if (user.getEmail() == null || !EMAIL_PATTERN.matcher(user.getEmail()).matches()) {
            violations.add("Valid email address is required");
        }
        if (user.getPhoneNumber() != null && !PHONE_PATTERN.matcher(user.getPhoneNumber()).matches()) {
            violations.add("Invalid phone number format");
        }
    }

    private void validatePassword(String password, List<String> violations) {
        if (password == null || !PASSWORD_PATTERN.matcher(password).matches()) {
            violations.add("Password must be at least 8 characters long and contain at least one " +
                    "uppercase letter, one lowercase letter, one number, and one special character");
        }
    }

    public void validateDeactivation(User user) {
        if (user.getStatus() == UserStatus.DEACTIVATED) {
            throw new UserValidationException("User is already deactivated");
        }
        // Add additional deactivation validation rules here
    }

    public void validateAccountDeletion(User user) {
        List<String> violations = new ArrayList<>();

        // Check for active orders
        if (user.hasActiveOrders()) {
            violations.add("Cannot delete user with active orders");
        }

        // Check for outstanding payments
        if (user.hasOutstandingPayments()) {
            violations.add("Cannot delete user with outstanding payments");
        }

        if (!violations.isEmpty()) {
            throw new UserValidationException(violations);
        }
    }
}
