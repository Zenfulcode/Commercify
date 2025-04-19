package com.zenfulcode.commercify.payment.domain.valueobject;

import com.zenfulcode.commercify.payment.domain.model.PaymentMethod;

public interface PaymentProviderRequest {
    PaymentMethod getPaymentMethod();
}

