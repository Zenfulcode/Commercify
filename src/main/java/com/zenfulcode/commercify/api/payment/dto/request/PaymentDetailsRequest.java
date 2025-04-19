package com.zenfulcode.commercify.api.payment.dto.request;

import java.util.Map;

public record PaymentDetailsRequest(
        String paymentMethod,
        String returnUrl,
        String cancelUrl,
        Map<String, String> additionalData
) {
}
