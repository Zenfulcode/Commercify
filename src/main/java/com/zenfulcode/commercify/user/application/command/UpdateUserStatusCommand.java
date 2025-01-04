package com.zenfulcode.commercify.user.application.command;

import com.zenfulcode.commercify.user.domain.model.UserStatus;
import com.zenfulcode.commercify.user.domain.valueobject.UserId;

public record UpdateUserStatusCommand(
        UserId userId,
        UserStatus newStatus
) {
}
