package com.zenfulcode.commercify.api.payment.request;

import com.zenfulcode.commercify.order.domain.valueobject.OrderId;

public record InitiatePaymentRequest(
        OrderId orderId,
        String paymentMethod,
        String provider,
        PaymentDetailsRequest paymentDetails
) {
}
