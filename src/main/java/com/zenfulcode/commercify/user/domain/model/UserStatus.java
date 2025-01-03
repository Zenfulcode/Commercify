package com.zenfulcode.commercify.user.domain.model;

public enum UserStatus {
    PENDING,    // Initial status after registration
    ACTIVE,     // Normal active user
    INACTIVE,   // User deactivated (by self or admin)
    SUSPENDED   // User suspended (by admin)
}
