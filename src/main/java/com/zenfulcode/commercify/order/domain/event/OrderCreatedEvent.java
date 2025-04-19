package com.zenfulcode.commercify.order.domain.event;

import com.zenfulcode.commercify.order.domain.valueobject.OrderId;
import com.zenfulcode.commercify.shared.domain.event.DomainEvent;
import com.zenfulcode.commercify.shared.domain.valueobject.AggregateId;
import com.zenfulcode.commercify.user.domain.valueobject.UserId;
import lombok.Getter;

import java.time.Instant;

@Getter
public class OrderCreatedEvent extends DomainEvent {
    @AggregateId
    private final OrderId orderId;
    private final UserId userId;
    private final String currency;
    private final Instant createdAt;

    public OrderCreatedEvent(Object source, OrderId orderId, UserId userId, String currency) {
        super(source);
        this.orderId = orderId;
        this.userId = userId;
        this.currency = currency;
        this.createdAt = Instant.now();
    }

    @Override
    public String getEventType() {
        return "ORDER_CREATED";
    }
}
