package com.zenfulcode.commercify.payment.application.dto;

import com.zenfulcode.commercify.payment.domain.valueobject.PaymentId;

import java.util.Map;

public record InitializedPayment(
        PaymentId paymentId,
        String redirectUrl,
        Map<String, Object> additionalData
) {}