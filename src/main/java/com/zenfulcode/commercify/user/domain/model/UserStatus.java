package com.zenfulcode.commercify.user.domain.model;

public enum UserStatus {
    PENDING,     // Initial state after registration
    ACTIVE,      // User is active and can use the system
    SUSPENDED,   // User is temporarily suspended
    DEACTIVATED  // User account is deactivated (terminal state)
}
