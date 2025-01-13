package com.zenfulcode.commercify.payment.domain.valueobject;

import java.util.Map;

public record PaymentProviderResponse(
        String providerReference,
        String redirectUrl,
        Map<String, Object> additionalData
) {}