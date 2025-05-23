package com.zenfulcode.commercify.product.domain.event;

import com.zenfulcode.commercify.product.domain.valueobject.ProductId;
import com.zenfulcode.commercify.shared.domain.event.DomainEvent;
import com.zenfulcode.commercify.shared.domain.valueobject.AggregateId;
import lombok.Getter;

@Getter
public class LargeStockIncreaseEvent extends DomainEvent {
    @AggregateId
    private final ProductId productId;
    private final int quantity;
    private final String reason;

    public LargeStockIncreaseEvent(Object source, ProductId productId, int quantity, String reason) {
        super(source);
        this.productId = productId;
        this.quantity = quantity;
        this.reason = reason;
    }

    @Override
    public String getEventType() {
        return "LARGE_STOCK_INCREASE";
    }
}
