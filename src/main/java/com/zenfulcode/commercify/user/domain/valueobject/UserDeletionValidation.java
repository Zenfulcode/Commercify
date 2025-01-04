package com.zenfulcode.commercify.user.domain.valueobject;

import java.util.List;

public record UserDeletionValidation(
        boolean canDelete,
        List<String> issues
) {}