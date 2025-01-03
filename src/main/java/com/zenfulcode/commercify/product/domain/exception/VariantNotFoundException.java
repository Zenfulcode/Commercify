package com.zenfulcode.commercify.product.domain.exception;

import com.zenfulcode.commercify.shared.domain.exception.EntityNotFoundException;

public class VariantNotFoundException extends EntityNotFoundException {
    public VariantNotFoundException(Object entityId) {
        super("Product variant", entityId);
    }
}
