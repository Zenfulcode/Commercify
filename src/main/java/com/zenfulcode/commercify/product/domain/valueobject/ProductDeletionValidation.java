package com.zenfulcode.commercify.product.domain.valueobject;

import java.util.List;

public record ProductDeletionValidation(
        boolean canDelete,
        List<String> issues
) {}