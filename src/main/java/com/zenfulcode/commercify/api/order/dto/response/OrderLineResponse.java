package com.zenfulcode.commercify.api.order.dto.response;

import com.zenfulcode.commercify.product.domain.valueobject.ProductId;
import com.zenfulcode.commercify.product.domain.valueobject.VariantId;

public record OrderLineResponse(
        String id,
        ProductId productId,
        VariantId variantId,
        int quantity,
        MoneyResponse unitPrice,
        MoneyResponse total
) {
}