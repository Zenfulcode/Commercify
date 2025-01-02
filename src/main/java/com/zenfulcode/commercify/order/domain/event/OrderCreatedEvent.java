package com.zenfulcode.commercify.order.domain.event;

import com.zenfulcode.commercify.order.domain.valueobject.OrderId;
import com.zenfulcode.commercify.shared.domain.event.DomainEvent;
import lombok.Getter;

import java.time.Instant;

@Getter
public class OrderCreatedEvent extends DomainEvent {
    private final OrderId orderId;
    private final Long userId;
    private final String currency;
    private final Instant createdAt;

    public OrderCreatedEvent(OrderId orderId, Long userId, String currency) {
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
