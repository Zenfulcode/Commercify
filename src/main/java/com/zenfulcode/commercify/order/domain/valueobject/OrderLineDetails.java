package com.zenfulcode.commercify.order.domain.valueobject;

import com.zenfulcode.commercify.order.domain.exception.OrderValidationException;
import com.zenfulcode.commercify.product.domain.valueobject.ProductId;
import com.zenfulcode.commercify.shared.domain.model.Money;

import java.util.ArrayList;
import java.util.List;

public record OrderLineDetails(
        ProductId productId,
        ProductId variantId,
        int quantity,
        Money unitPrice
) {
    public OrderLineDetails {
        validate(productId, quantity);
    }

    private void validate(ProductId productId, int quantity) {
        List<String> violations = new ArrayList<>();

        if (productId == null) {
            violations.add("Product ID is required");
        }

        if (quantity <= 0) {
            violations.add("Quantity must be greater than zero");
        }

        if (!violations.isEmpty()) {
            throw new OrderValidationException(violations);
        }
    }

    public Money calculateTotal() {
        return unitPrice.multiply(quantity);
    }

    public boolean hasVariant() {
        return variantId != null;
    }
}
