package com.zenfulcode.commercify.payment.domain.service.provider;

import com.zenfulcode.commercify.order.domain.valueobject.OrderId;
import com.zenfulcode.commercify.payment.domain.exception.PaymentValidationException;
import com.zenfulcode.commercify.payment.domain.model.Payment;
import com.zenfulcode.commercify.payment.domain.model.PaymentMethod;
import com.zenfulcode.commercify.payment.domain.model.PaymentProvider;
import com.zenfulcode.commercify.payment.domain.service.PaymentDomainService;
import com.zenfulcode.commercify.payment.domain.service.PaymentProviderService;
import com.zenfulcode.commercify.payment.domain.valueobject.*;
import com.zenfulcode.commercify.payment.domain.valueobject.webhook.MobilepayWebhookPayload;
import com.zenfulcode.commercify.payment.domain.valueobject.webhook.WebhookPayload;
import com.zenfulcode.commercify.payment.infrastructure.gateway.MobilepayCreatePaymentRequest;
import com.zenfulcode.commercify.payment.infrastructure.gateway.MobilepayPaymentResponse;
import com.zenfulcode.commercify.payment.infrastructure.gateway.client.MobilepayClient;
import com.zenfulcode.commercify.shared.domain.model.Money;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class MobilepayProviderService implements PaymentProviderService {
    private final MobilepayClient mobilePayClient;
    private final PaymentDomainService paymentService;

    @Override
    public PaymentProviderResponse initiatePayment(Payment payment, OrderId orderId, PaymentProviderRequest request) {
        // Convert to MobilePay specific request
        MobilepayPaymentRequest mobilePayRequest = (MobilepayPaymentRequest) request;

        // Create payment through MobilePay client
        MobilepayPaymentResponse response = mobilePayClient.createPayment(
                new MobilepayCreatePaymentRequest(
                        payment.getAmount(),
                        mobilePayRequest.getPaymentMethod(),
                        mobilePayRequest.phoneNumber(),
                        mobilePayRequest.returnUrl(),
                        orderId.toString()
                )
        );

        // Return provider response
        return new PaymentProviderResponse(
                response.reference(),
                response.redirectUrl(),
                Map.of("providerReference", response.reference())
        );
    }

    @Override
    public void handleCallback(Payment payment, WebhookPayload payload) {
        MobilepayWebhookPayload webhookPayload = (MobilepayWebhookPayload) payload;

        log.info("Handling MobilePay webhook: {}", webhookPayload);

        if (!webhookPayload.isValid()) {
            log.error("Invalid webhook payload: {}", webhookPayload);
            return;
        }

        switch (payload.getEventType()) {
            case "CREATED":
                log.info("Payment created: {}", payment.getId());
                break;
            case "AUTHORIZED":
                paymentService.authorizePayment(payment);
                break;
            case "ABORTED", "CANCELLED":
                paymentService.cancelPayment(payment);
                break;
            case "EXPIRED":
                paymentService.failPayment(payment, "Payment expired", PaymentStatus.EXPIRED);
                break;
            case "TERMINATED":
                paymentService.failPayment(payment, "Payment terminated", PaymentStatus.TERMINATED);
                break;
            case "CAPTURED":
                double amount = webhookPayload.amount().value() / 100.0;
                Money captureAmount = Money.of(amount, webhookPayload.amount().currency());
                paymentService.capturePayment(payment, TransactionId.generate(), captureAmount);
                break;
            case "REFUNDED":
                log.info("Handle refund: {}", payment.getId());
                break;
        }
    }

    @Override
    public Set<PaymentMethod> getSupportedPaymentMethods() {
        return Set.of(PaymentMethod.WALLET);
    }

    @Override
    public PaymentProviderConfig getProviderConfig() {
        return new PaymentProviderConfig(
                PaymentProvider.MOBILEPAY,
                true,
                new HashMap<>()
        );
    }

    @Override
    public void validateRequest(PaymentProviderRequest request) {
        MobilepayPaymentRequest mobilePayRequest = (MobilepayPaymentRequest) request;
        List<String> violations = new ArrayList<>();

        // TODO Make sure the phone number is valid (DK, FI, SE or NO)
        if (mobilePayRequest.phoneNumber() == null || mobilePayRequest.phoneNumber().isBlank()) {
            violations.add("Phone number is required");
        }

        if (mobilePayRequest.returnUrl() == null || mobilePayRequest.returnUrl().isBlank()) {
            violations.add("Return URL is required");
        }

        if (!violations.isEmpty()) {
            throw new PaymentValidationException("Mobilepay validation: ", violations);
        }
    }

    @Override
    public boolean supportsPaymentMethod(PaymentMethod method) {
        return getSupportedPaymentMethods().contains(method);
    }

    public void registerWebhook(String callbackUrl) {
        mobilePayClient.registerWebhook(callbackUrl);
    }

    @Override
    public void deleteWebhook(String webhookId) {
        mobilePayClient.deleteWebhook(webhookId);
    }

    @Override
    public Object getWebhooks() {
        return mobilePayClient.getWebhooks();
    }

    public void authenticateWebhook(String date, String contentSha256, String authorization, String payload) {
        mobilePayClient.validateWebhook(contentSha256, authorization, date, payload);
    }
}
