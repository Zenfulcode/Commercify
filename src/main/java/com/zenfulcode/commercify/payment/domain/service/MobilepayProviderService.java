package com.zenfulcode.commercify.payment.domain.service;

import com.zenfulcode.commercify.order.domain.valueobject.OrderId;
import com.zenfulcode.commercify.payment.domain.model.Payment;
import com.zenfulcode.commercify.payment.domain.model.PaymentMethod;
import com.zenfulcode.commercify.payment.domain.valueobject.PaymentProviderConfig;
import com.zenfulcode.commercify.payment.domain.valueobject.PaymentProviderRequest;
import com.zenfulcode.commercify.payment.domain.valueobject.PaymentProviderResponse;
import com.zenfulcode.commercify.payment.domain.valueobject.webhook.WebhookPayload;

import java.util.Set;

public class MobilepayProviderService implements PaymentProviderService {
    @Override
    public PaymentProviderResponse initiatePayment(Payment payment, OrderId orderId, PaymentProviderRequest request) {
        return null;
    }

    @Override
    public void handleCallback(WebhookPayload payload) {

    }

    @Override
    public Set<PaymentMethod> getSupportedPaymentMethods() {
        return Set.of(PaymentMethod.WALLET);
    }

    @Override
    public PaymentProviderConfig getProviderConfig() {
        return null;
    }

    @Override
    public void validateRequest(PaymentProviderRequest request) {

    }

    @Override
    public boolean supportsPaymentMethod(PaymentMethod method) {
        return false;
    }
}
