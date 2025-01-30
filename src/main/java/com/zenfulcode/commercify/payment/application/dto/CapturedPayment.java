package com.zenfulcode.commercify.payment.application.dto;

public record CapturedPayment(
        String transactionId,
        CaptureAmount captureAmount,
        boolean isFullyCaptured
) {
}
