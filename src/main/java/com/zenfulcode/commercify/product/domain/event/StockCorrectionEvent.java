package com.zenfulcode.commercify.product.domain.event;

import com.zenfulcode.commercify.product.domain.valueobject.ProductId;
import com.zenfulcode.commercify.shared.domain.event.DomainEvent;
import lombok.Getter;

@Getter
public class StockCorrectionEvent extends DomainEvent {
    private final ProductId productId;
    private final int quantity;
    private final String reason;

    public StockCorrectionEvent(ProductId productId, int quantity, String reason) {
        this.productId = productId;
        this.quantity = quantity;
        this.reason = reason;
    }
}
