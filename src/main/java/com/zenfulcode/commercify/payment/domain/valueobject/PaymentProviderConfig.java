package com.zenfulcode.commercify.payment.domain.valueobject;

import java.util.Map;

public record PaymentProviderConfig(String provider, boolean isActive, Map<String, String> config) {
}

