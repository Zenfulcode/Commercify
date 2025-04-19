package com.zenfulcode.commercify.api.payment.dto.response;

import com.zenfulcode.commercify.payment.application.dto.CaptureAmount;
import com.zenfulcode.commercify.payment.domain.valueobject.TransactionId;

public record CapturedPaymentResponse(
        TransactionId transactionId,
        CaptureAmount captureAmount,
        boolean isFullyCaptured
) {
}
