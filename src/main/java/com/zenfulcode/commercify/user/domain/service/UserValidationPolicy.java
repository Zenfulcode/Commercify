package com.zenfulcode.commercify.user.domain.service;

import com.zenfulcode.commercify.user.domain.model.User;
import com.zenfulcode.commercify.user.domain.model.UserStatus;

public interface UserValidationPolicy {
    boolean canTransitionToStatus(User user, UserStatus newStatus);
}
