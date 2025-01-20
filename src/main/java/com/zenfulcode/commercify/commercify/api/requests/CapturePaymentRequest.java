package com.zenfulcode.commercify.commercify.api.requests;

public record CapturePaymentRequest(double captureAmount, boolean isPartialCapture) {
}
