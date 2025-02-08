package com.zenfulcode.commercify.api.payment.dto.request;

import com.zenfulcode.commercify.order.domain.valueobject.OrderId;

public record InitiatePaymentRequest(
        String orderId,
        String provider,
        PaymentDetailsRequest paymentDetails
) {
    public OrderId getOrderId() {
        return OrderId.of(orderId);
    }
}
