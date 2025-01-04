package com.zenfulcode.commercify.api.product.dto.response;

import com.zenfulcode.commercify.product.domain.valueobject.ProductId;

public record CreateProductResponse(ProductId productId, String message) {
}
