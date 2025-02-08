package com.zenfulcode.commercify.api.payment.dto.response;

import java.util.Map;

public record PaymentResponse(
        String paymentId,
        String redirectUrl,
        Map<String, Object> additionalData
) {
}
