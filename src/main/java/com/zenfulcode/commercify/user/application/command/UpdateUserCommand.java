package com.zenfulcode.commercify.user.application.command;

import com.zenfulcode.commercify.user.domain.valueobject.UserId;
import com.zenfulcode.commercify.user.domain.valueobject.UserSpecification;

public record UpdateUserCommand(
        UserId userId,
        UserSpecification userSpec
) {
}