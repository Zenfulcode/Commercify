package com.zenfulcode.commercify.payment.application.dto;

import com.zenfulcode.commercify.payment.domain.valueobject.TransactionId;

public record CapturedPayment(
        TransactionId transactionId,
        CaptureAmount captureAmount,
        boolean isFullyCaptured
) {
}
