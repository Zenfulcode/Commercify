package com.zenfulcode.commercify.payment.domain.valueobject;

import com.zenfulcode.commercify.payment.domain.model.PaymentMethod;

public record MobilepayPaymentRequest(
        PaymentMethod paymentMethod,
        String phoneNumber,
        String returnUrl
) implements PaymentProviderRequest {
    @Override
    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }
}
