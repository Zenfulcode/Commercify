package com.zenfulcode.commercify.payment.domain.event;

import com.zenfulcode.commercify.order.domain.valueobject.OrderId;
import com.zenfulcode.commercify.payment.domain.valueobject.PaymentId;
import com.zenfulcode.commercify.payment.domain.valueobject.refund.RefundReason;
import com.zenfulcode.commercify.shared.domain.event.DomainEvent;
import com.zenfulcode.commercify.shared.domain.model.Money;
import com.zenfulcode.commercify.shared.domain.valueobject.AggregateId;
import lombok.Getter;

import java.time.Instant;

/**
 * Event raised when payment is refunded
 */
@Getter
public class IssuedRefundEvent extends DomainEvent {
    // Getters
    @AggregateId
    private final PaymentId paymentId;
    private final OrderId orderId;
    private final Money refundAmount;
    private final RefundReason reason;
    private final String notes;
    private final boolean isFullRefund;
    private final Instant refundedAt;

    public IssuedRefundEvent(
            Object source,
            PaymentId paymentId,
            OrderId orderId,
            Money refundAmount,
            RefundReason reason,
            String notes,
            boolean isFullRefund
    ) {
        super(source);
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.refundAmount = refundAmount;
        this.reason = reason;
        this.notes = notes;
        this.isFullRefund = isFullRefund;
        this.refundedAt = Instant.now();
    }

    @Override
    public String getEventType() {
        return "ISSUED_REFUND";
    }
}
