package com.zenfulcode.commercify.api.order.dto.response;

import com.zenfulcode.commercify.product.domain.valueobject.ProductId;
import com.zenfulcode.commercify.product.domain.valueobject.VariantId;
import com.zenfulcode.commercify.shared.domain.model.Money;

public record OrderLineResponse(
        String id,
        ProductId productId,
        VariantId variantId,
        int quantity,
        Money unitPrice,
        Money total
) {
}