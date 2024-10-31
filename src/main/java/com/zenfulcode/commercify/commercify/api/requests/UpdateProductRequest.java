package com.zenfulcode.commercify.commercify.api.requests;

import java.util.List;

public record UpdateProductRequest(
        String name,
        String description,
        Integer stock,
        String imageUrl,
        Boolean active,
        List<UpdatePriceRequest> prices
) {
    // Validates that at least one field is present for update
    public boolean hasUpdates() {
        return name != null || description != null || stock != null ||
                imageUrl != null || active != null ||
                (prices != null && !prices.isEmpty());
    }
}