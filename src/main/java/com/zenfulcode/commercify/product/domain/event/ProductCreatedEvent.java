package com.zenfulcode.commercify.product.domain.event;

import com.zenfulcode.commercify.product.domain.valueobject.ProductId;
import com.zenfulcode.commercify.shared.domain.event.DomainEvent;
import com.zenfulcode.commercify.shared.domain.model.Money;
import com.zenfulcode.commercify.shared.domain.valueobject.AggregateId;
import lombok.Getter;

@Getter
public class ProductCreatedEvent extends DomainEvent {
    @AggregateId
    private final ProductId productId;
    private final String name;
    private final Money price;

    public ProductCreatedEvent(Object source, ProductId productId, String name, Money price) {
        super(source);
        this.productId = productId;
        this.name = name;
        this.price = price;
    }

    @Override
    public String getEventType() {
        return "PRODUCT_CREATED";
    }
}
