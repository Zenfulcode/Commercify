package com.zenfulcode.commercify.order.domain.exception;

import com.zenfulcode.commercify.order.domain.model.OrderStatus;
import com.zenfulcode.commercify.order.domain.valueobject.OrderId;
import com.zenfulcode.commercify.shared.domain.exception.DomainException;
import lombok.Getter;

@Getter
public class InvalidOrderStateTransitionException extends DomainException {
    private final OrderId orderId;
    private final OrderStatus currentStatus;
    private final OrderStatus targetStatus;

    public InvalidOrderStateTransitionException(
            OrderId orderId,
            OrderStatus currentStatus,
            OrderStatus targetStatus,
            String message
    ) {
        super(String.format("Invalid order status transition from %s to %s for order %s: %s",
                currentStatus, targetStatus, orderId, message));

        this.orderId = orderId;
        this.currentStatus = currentStatus;
        this.targetStatus = targetStatus;
    }

}
