package com.zenfulcode.commercify.api.payment.request;

import com.zenfulcode.commercify.order.domain.valueobject.OrderId;

public record InitiatePaymentRequest(
        OrderId orderId,
        String provider,
        PaymentDetailsRequest paymentDetails
) {
}
