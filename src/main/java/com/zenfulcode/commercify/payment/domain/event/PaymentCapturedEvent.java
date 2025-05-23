package com.zenfulcode.commercify.payment.domain.event;

import com.zenfulcode.commercify.order.domain.valueobject.OrderId;
import com.zenfulcode.commercify.payment.domain.valueobject.PaymentId;
import com.zenfulcode.commercify.payment.domain.valueobject.TransactionId;
import com.zenfulcode.commercify.shared.domain.event.DomainEvent;
import com.zenfulcode.commercify.shared.domain.model.Money;
import com.zenfulcode.commercify.shared.domain.valueobject.AggregateId;
import lombok.Getter;

import java.time.Instant;

/**
 * Event raised when payment is captured
 */
@Getter
public class PaymentCapturedEvent extends DomainEvent {
    // Getters
    @AggregateId
    private final PaymentId paymentId;
    private final OrderId orderId;
    private final Money amount;
    private final TransactionId transactionId;
    private final Instant capturedAt;

    public PaymentCapturedEvent(
            Object source,
            PaymentId paymentId,
            OrderId orderId,
            Money amount,
            TransactionId transactionId
    ) {
        super(source);
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.amount = amount;
        this.transactionId = transactionId;
        this.capturedAt = Instant.now();
    }

    @Override
    public String getEventType() {
        return "PAYMENT_CAPTURED";
    }
}