package com.zenfulcode.commercify.api.product.dto.response;

public record ProductSummaryResponse(
        String id,
        String name,
        String description,
        String imageUrl,
        ProductPriceResponse price,
        int stock
) {
    public record ProductPriceResponse(
            double amount,
            String currency
    ) {
    }

    public record ProductInventoryResponse(
            int quantity,
            String status,
            boolean backorderable,
            Integer reorderPoint
    ) {
    }
}
