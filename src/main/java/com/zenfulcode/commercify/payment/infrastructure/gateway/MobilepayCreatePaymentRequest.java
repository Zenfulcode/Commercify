package com.zenfulcode.commercify.payment.infrastructure.gateway;

import com.zenfulcode.commercify.payment.domain.model.PaymentMethod;
import com.zenfulcode.commercify.shared.domain.model.Money;

public record MobilepayCreatePaymentRequest(
        Money amount,
        PaymentMethod paymentMethod,
        String phoneNumber,
        String returnUrl,
        String orderId
) {
}
