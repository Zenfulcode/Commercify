package com.zenfulcode.commercify.product.domain.event;


import com.zenfulcode.commercify.product.domain.valueobject.ProductId;
import com.zenfulcode.commercify.shared.domain.event.DomainEvent;
import com.zenfulcode.commercify.shared.domain.model.Money;
import com.zenfulcode.commercify.shared.domain.valueobject.AggregateId;
import lombok.Getter;

@Getter
public class ProductPriceUpdatedEvent extends DomainEvent {
    @AggregateId
    private final ProductId productId;
    private final Money newPrice;

    public ProductPriceUpdatedEvent(ProductId productId, Money newPrice) {
        this.productId = productId;
        this.newPrice = newPrice;
    }
}