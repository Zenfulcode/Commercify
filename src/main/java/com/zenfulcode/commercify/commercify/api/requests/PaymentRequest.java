package com.zenfulcode.commercify.commercify.api.requests;

public record PaymentRequest(Long orderId,
                             String currency,
                             String paymentMethod,
                             String returnUrl,
                             String phoneNumber) {
}