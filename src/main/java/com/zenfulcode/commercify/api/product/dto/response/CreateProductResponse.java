package com.zenfulcode.commercify.api.product.dto.response;

import com.zenfulcode.commercify.product.domain.valueobject.ProductId;

public record CreateProductResponse(String productId, String message) {
    public ProductId getProductId() {
        return ProductId.of(productId);
    }
}
