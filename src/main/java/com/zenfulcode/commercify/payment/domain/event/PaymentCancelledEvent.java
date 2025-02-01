package com.zenfulcode.commercify.payment.domain.event;

import com.zenfulcode.commercify.order.domain.valueobject.OrderId;
import com.zenfulcode.commercify.payment.domain.valueobject.PaymentId;
import com.zenfulcode.commercify.shared.domain.event.DomainEvent;
import com.zenfulcode.commercify.shared.domain.valueobject.AggregateId;
import lombok.Getter;

import java.time.Instant;

/**
 * Event raised when payment is cancelled
 */
@Getter
public class PaymentCancelledEvent extends DomainEvent {
    // Getters
    @AggregateId
    private final PaymentId paymentId;
    private final OrderId orderId;
    private final String reason;
    private final Instant cancelledAt;

    public PaymentCancelledEvent(
            Object source,
            PaymentId paymentId,
            OrderId orderId,
            String reason
    ) {
        super(source);
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.reason = reason;
        this.cancelledAt = Instant.now();
    }
}
