package com.zenfulcode.commercify.payment.application.command;

import com.zenfulcode.commercify.payment.domain.valueobject.PaymentId;
import com.zenfulcode.commercify.shared.domain.model.Money;

public record CapturePaymentCommand(
        PaymentId paymentId,
        Money captureAmount
) {
}
