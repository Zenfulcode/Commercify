package com.zenfulcode.commercify.product.domain.valueobject;

import com.zenfulcode.commercify.shared.domain.model.Money;

public record ProductUpdateSpec(
        String name,
        String description,
        Integer stock,
        Money price,
        Boolean active
) {
    public boolean hasNameUpdate() {
        return name != null;
    }

    public boolean hasDescriptionUpdate() {
        return description != null;
    }

    public boolean hasStockUpdate() {
        return stock != null;
    }

    public boolean hasPriceUpdate() {
        return price != null;
    }

    public boolean hasActiveUpdate() {
        return active != null;
    }
}