package com.zenfulcode.commercify.user.domain.exception;

import com.zenfulcode.commercify.shared.domain.exception.DomainException;
import com.zenfulcode.commercify.user.domain.model.UserStatus;
import com.zenfulcode.commercify.user.domain.valueobject.UserId;
import lombok.Getter;

@Getter
public class InvalidUserStateTransitionException extends DomainException {
    private final UserId userId;
    private final UserStatus currentStatus;
    private final UserStatus targetStatus;

    public InvalidUserStateTransitionException(
            UserId userId,
            UserStatus currentStatus,
            UserStatus targetStatus,
            String message
    ) {
        super(String.format("Invalid user status transition from %s to %s for user %s: %s",
                currentStatus, targetStatus, userId, message));

        this.userId = userId;
        this.currentStatus = currentStatus;
        this.targetStatus = targetStatus;
    }
}
