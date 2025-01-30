package com.zenfulcode.commercify.payment.application.dto;

import java.math.BigDecimal;

public record CaptureAmount(
        BigDecimal capturedAmount,
        BigDecimal remainingAmount,
        String currency
) {
}
