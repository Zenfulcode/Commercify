package com.zenfulcode.commercify.payment.domain.exception;

import com.zenfulcode.commercify.shared.domain.exception.EntityNotFoundException;

public class PaymentNotFoundException extends EntityNotFoundException {
    public PaymentNotFoundException(Object entityId) {
        super("Payment", entityId);
    }
}
