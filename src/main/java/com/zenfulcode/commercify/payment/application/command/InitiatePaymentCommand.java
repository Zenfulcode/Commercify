package com.zenfulcode.commercify.payment.application.command;

import com.zenfulcode.commercify.order.domain.model.Order;
import com.zenfulcode.commercify.payment.domain.model.PaymentMethod;
import com.zenfulcode.commercify.payment.domain.model.PaymentProvider;
import com.zenfulcode.commercify.payment.domain.valueobject.PaymentProviderRequest;

public record InitiatePaymentCommand(
        Order order,
        PaymentMethod paymentMethod,
        PaymentProvider provider,
        PaymentProviderRequest providerRequest
) {
}