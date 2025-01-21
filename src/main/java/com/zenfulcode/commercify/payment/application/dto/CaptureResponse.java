package com.zenfulcode.commercify.payment.application.dto;

import com.zenfulcode.commercify.shared.domain.model.Money;

public record CaptureResponse(
        String transactionId,
        Money capturedAmount
) {
}
