package com.zenfulcode.commercify.payment.infrastructure.gateway;

public record MobilepayPaymentResponse(
        String redirectUrl,
        String reference
) {
}
