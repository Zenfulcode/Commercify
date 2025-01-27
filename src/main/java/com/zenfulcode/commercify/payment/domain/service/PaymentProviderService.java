package com.zenfulcode.commercify.payment.domain.service;

import com.zenfulcode.commercify.order.domain.valueobject.OrderId;
import com.zenfulcode.commercify.payment.domain.model.Payment;
import com.zenfulcode.commercify.payment.domain.model.PaymentMethod;
import com.zenfulcode.commercify.payment.domain.valueobject.PaymentProviderConfig;
import com.zenfulcode.commercify.payment.domain.valueobject.PaymentProviderRequest;
import com.zenfulcode.commercify.payment.domain.valueobject.PaymentProviderResponse;
import com.zenfulcode.commercify.payment.domain.valueobject.webhook.WebhookPayload;

import java.util.Set;

/**
 * Base interface for all payment provider integrations
 */
public interface PaymentProviderService {
    /**
     * Initialize a payment with the provider
     */
    PaymentProviderResponse initiatePayment(Payment payment, OrderId orderId, PaymentProviderRequest request);

    /**
     * Handle provider webhook callbacks
     */
    void handleCallback(WebhookPayload payload);

    /**
     * Get supported payment methods
     */
    Set<PaymentMethod> getSupportedPaymentMethods();

    /**
     * Get provider-specific configuration
     */
    PaymentProviderConfig getProviderConfig();

    /**
     * Validate provider-specific request
     */
    void validateRequest(PaymentProviderRequest request);

    /**
     * Check if provider supports payment method
     */
    boolean supportsPaymentMethod(PaymentMethod method);

    /**
     * Register a webhook with the provider
     */
    void registerWebhook(String callbackUrl);

    /**
     * Delete a webhook with the provider
     */
    void deleteWebhook(String webhookId);

    /**
     * Get all webhooks registered with the provider
     */
    Object getWebhooks();
}
