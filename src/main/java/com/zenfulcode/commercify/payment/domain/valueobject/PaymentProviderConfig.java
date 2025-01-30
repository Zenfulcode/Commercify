package com.zenfulcode.commercify.payment.domain.valueobject;

import com.zenfulcode.commercify.payment.domain.model.PaymentProvider;

import java.util.Map;

public record PaymentProviderConfig(PaymentProvider provider, boolean isActive, Map<String, String> config) {
}

