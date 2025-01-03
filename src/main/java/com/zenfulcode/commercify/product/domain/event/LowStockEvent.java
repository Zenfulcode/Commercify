package com.zenfulcode.commercify.product.domain.event;

import com.zenfulcode.commercify.product.domain.valueobject.ProductId;
import com.zenfulcode.commercify.shared.domain.event.DomainEvent;
import com.zenfulcode.commercify.shared.domain.valueobject.AggregateId;
import lombok.Getter;

@Getter
public class LowStockEvent extends DomainEvent {
    @AggregateId
    private final ProductId productId;
    private final int stockAmount;

    public LowStockEvent(ProductId productId, int stockAmount) {
        this.productId = productId;
        this.stockAmount = stockAmount;
    }

}
