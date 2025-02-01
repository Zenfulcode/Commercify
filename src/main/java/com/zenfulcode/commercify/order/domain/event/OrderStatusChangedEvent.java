package com.zenfulcode.commercify.order.domain.event;

import com.zenfulcode.commercify.order.domain.model.OrderStatus;
import com.zenfulcode.commercify.order.domain.valueobject.OrderId;
import com.zenfulcode.commercify.shared.domain.event.DomainEvent;
import com.zenfulcode.commercify.shared.domain.valueobject.AggregateId;
import lombok.Getter;

import java.time.Instant;

@Getter
public class OrderStatusChangedEvent extends DomainEvent {
    @AggregateId
    private final OrderId orderId;
    private final OrderStatus oldStatus;
    private final OrderStatus newStatus;
    private final Instant changedAt;

    public OrderStatusChangedEvent(
            Object source,
            OrderId orderId,
            OrderStatus oldStatus,
            OrderStatus newStatus
    ) {
        super(source);
        this.orderId = orderId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.changedAt = Instant.now();
    }

    @Override
    public String getEventType() {
        return "ORDER_STATUS_CHANGED";
    }

    public boolean isCompletedTransition() {
        return newStatus == OrderStatus.COMPLETED;
    }

    public boolean isCancellationTransition() {
        return newStatus == OrderStatus.CANCELLED;
    }

    public boolean isShippingTransition() {
        return newStatus == OrderStatus.SHIPPED;
    }
}
