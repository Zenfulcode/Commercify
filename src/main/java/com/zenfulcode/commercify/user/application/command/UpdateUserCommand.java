package com.zenfulcode.commercify.user.application.command;

import com.zenfulcode.commercify.user.application.dto.UserUpdateSpec;
import com.zenfulcode.commercify.user.domain.valueobject.UserId;

public record UpdateUserCommand(
        UserId userId,
        UserUpdateSpec updateSpec
) {}