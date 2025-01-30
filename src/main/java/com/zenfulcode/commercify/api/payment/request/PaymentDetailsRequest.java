package com.zenfulcode.commercify.api.payment.request;

import java.util.Map;

public record PaymentDetailsRequest(
        String paymentMethod,
        String returnUrl,
        String cancelUrl,
        Map<String, String> additionalData
) {
}
