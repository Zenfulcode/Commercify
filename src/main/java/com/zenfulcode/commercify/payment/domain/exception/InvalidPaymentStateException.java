package com.zenfulcode.commercify.payment.domain.exception;

import com.zenfulcode.commercify.payment.domain.valueobject.PaymentStatus;
import com.zenfulcode.commercify.payment.domain.valueobject.PaymentId;
import com.zenfulcode.commercify.shared.domain.exception.DomainException;
import lombok.Getter;

/**
 * Thrown when payment is in invalid state for requested operation
 */
@Getter
public class InvalidPaymentStateException extends DomainException {
    private final PaymentId paymentId;
    private final PaymentStatus currentStatus;
    private final PaymentStatus targetStatus;
    private final String reason;

    public InvalidPaymentStateException(
            PaymentId paymentId,
            PaymentStatus currentStatus,
            PaymentStatus targetStatus,
            String reason
    ) {
        super(String.format(
                "Cannot transition payment %s from %s to %s: %s",
                paymentId,
                currentStatus,
                targetStatus,
                reason
        ));
        this.paymentId = paymentId;
        this.currentStatus = currentStatus;
        this.targetStatus = targetStatus;
        this.reason = reason;
    }
}
