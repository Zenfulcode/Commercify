package com.zenfulcode.commercify.product.domain.exception;

import com.zenfulcode.commercify.product.domain.valueobject.ProductId;
import com.zenfulcode.commercify.shared.domain.exception.EntityNotFoundException;

public class ProductNotFoundException extends EntityNotFoundException {
    public ProductNotFoundException(ProductId productId) {
        super("Product", productId);
    }
}