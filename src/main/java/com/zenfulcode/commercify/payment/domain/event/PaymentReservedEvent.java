package com.zenfulcode.commercify.payment.domain.event;

import com.zenfulcode.commercify.order.domain.valueobject.OrderId;
import com.zenfulcode.commercify.payment.domain.valueobject.PaymentId;
import com.zenfulcode.commercify.payment.domain.valueobject.TransactionId;
import com.zenfulcode.commercify.shared.domain.event.DomainEvent;
import lombok.Getter;

@Getter
public class PaymentReservedEvent extends DomainEvent {
    private final PaymentId paymentId;
    private final OrderId orderId;
    private final TransactionId transactionId;

    public PaymentReservedEvent(PaymentId paymentId, OrderId orderId, TransactionId transactionId) {
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.transactionId = transactionId;
    }

}
