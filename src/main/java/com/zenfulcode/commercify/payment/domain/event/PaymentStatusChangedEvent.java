package com.zenfulcode.commercify.payment.domain.event;

import com.zenfulcode.commercify.order.domain.valueobject.OrderId;
import com.zenfulcode.commercify.payment.domain.valueobject.PaymentId;
import com.zenfulcode.commercify.payment.domain.valueobject.PaymentStatus;
import com.zenfulcode.commercify.payment.domain.valueobject.TransactionId;
import com.zenfulcode.commercify.shared.domain.event.DomainEvent;
import com.zenfulcode.commercify.shared.domain.valueobject.AggregateId;
import lombok.Getter;

import java.time.Instant;

/**
 * Event raised when payment status changes
 */
@Getter
public class PaymentStatusChangedEvent extends DomainEvent {
    // Getters
    @AggregateId
    private final PaymentId paymentId;
    private final OrderId orderId;
    private final PaymentStatus oldStatus;
    private final PaymentStatus newStatus;
    @AggregateId
    private final TransactionId transactionId;
    private final Instant changedAt;

    public PaymentStatusChangedEvent(
            Object source,
            PaymentId paymentId,
            OrderId orderId,
            PaymentStatus oldStatus,
            PaymentStatus newStatus,
            TransactionId transactionId
    ) {
        super(source);
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.transactionId = transactionId;
        this.changedAt = Instant.now();
    }

    public boolean isCompletedTransition() {
        return newStatus == PaymentStatus.CAPTURED;
    }

    public boolean isFailedTransition() {
        return newStatus == PaymentStatus.FAILED;
    }
}