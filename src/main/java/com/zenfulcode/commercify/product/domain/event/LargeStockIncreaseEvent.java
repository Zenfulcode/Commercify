package com.zenfulcode.commercify.product.domain.event;

import com.zenfulcode.commercify.product.domain.valueobject.ProductId;
import com.zenfulcode.commercify.shared.domain.event.DomainEvent;
import lombok.Getter;

@Getter
public class LargeStockIncreaseEvent extends DomainEvent {
    private final ProductId productId;
    private final int quantity;
    private final String reason;

    public LargeStockIncreaseEvent(ProductId productId, int quantity, String reason) {
        this.productId = productId;
        this.quantity = quantity;
        this.reason = reason;
    }
}
