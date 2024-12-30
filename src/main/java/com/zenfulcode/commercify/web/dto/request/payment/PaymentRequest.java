package com.zenfulcode.commercify.web.dto.request.payment;

public record PaymentRequest(Long orderId,
                             String currency,
                             String paymentMethod,
                             String returnUrl,
                             String phoneNumber) {
}