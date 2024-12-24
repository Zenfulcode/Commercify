package com.zenfulcode.commercify.commercify.integration;

import com.zenfulcode.commercify.commercify.api.requests.PaymentRequest;
import com.zenfulcode.commercify.commercify.api.responses.PaymentResponse;

public interface IPaymentProvider {
    PaymentResponse initiatePayment(PaymentRequest request) throws Exception;

    void handlePaymentCallback(String paymentReference, String status) throws Exception;

    WebhookResponse registerWebhooks(String callbackUrl) throws Exception;
}
