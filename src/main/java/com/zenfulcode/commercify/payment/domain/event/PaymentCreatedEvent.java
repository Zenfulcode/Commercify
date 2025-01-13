package com.zenfulcode.commercify.payment.domain.event;

import com.zenfulcode.commercify.order.domain.valueobject.OrderId;
import com.zenfulcode.commercify.payment.domain.model.PaymentMethod;
import com.zenfulcode.commercify.payment.domain.model.PaymentProvider;
import com.zenfulcode.commercify.payment.domain.valueobject.PaymentId;
import com.zenfulcode.commercify.shared.domain.event.DomainEvent;
import com.zenfulcode.commercify.shared.domain.model.Money;
import com.zenfulcode.commercify.shared.domain.valueobject.AggregateId;
import lombok.Getter;

import java.time.Instant;

/**
 * Event raised when a payment is created
 */
@Getter
public class PaymentCreatedEvent extends DomainEvent {
    // Getters
    @AggregateId
    private final PaymentId paymentId;
    private final OrderId orderId;
    private final Money amount;
    private final PaymentMethod paymentMethod;
    private final PaymentProvider provider;
    private final Instant createdAt;

    public PaymentCreatedEvent(
            PaymentId paymentId,
            OrderId orderId,
            Money amount,
            PaymentMethod paymentMethod,
            PaymentProvider provider
    ) {
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.provider = provider;
        this.createdAt = Instant.now();
    }
}
