package com.zenfulcode.commercify.payment.domain.event;

import com.zenfulcode.commercify.order.domain.valueobject.OrderId;
import com.zenfulcode.commercify.payment.domain.valueobject.PaymentId;
import com.zenfulcode.commercify.shared.domain.event.DomainEvent;
import com.zenfulcode.commercify.shared.domain.valueobject.AggregateId;
import lombok.Getter;

import java.time.Instant;

/**
 * Event raised when payment fails
 */
@Getter
public class PaymentFailedEvent extends DomainEvent {
    // Getters
    @AggregateId
    private final PaymentId paymentId;
    private final OrderId orderId;
    private final String errorMessage;
    private final Instant failedAt;

    public PaymentFailedEvent(
            Object source,
            PaymentId paymentId,
            OrderId orderId,
            String errorMessage
    ) {
        super(source);
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.errorMessage = errorMessage;
        this.failedAt = Instant.now();
    }
}
