package com.zenfulcode.commercify.payment.application.service;

import com.zenfulcode.commercify.payment.application.command.CapturePaymentCommand;
import com.zenfulcode.commercify.payment.application.command.InitiatePaymentCommand;
import com.zenfulcode.commercify.payment.application.dto.CaptureAmount;
import com.zenfulcode.commercify.payment.application.dto.CapturedPayment;
import com.zenfulcode.commercify.payment.application.dto.InitializedPayment;
import com.zenfulcode.commercify.payment.domain.exception.PaymentProviderNotFoundException;
import com.zenfulcode.commercify.payment.domain.model.Payment;
import com.zenfulcode.commercify.payment.domain.model.PaymentProvider;
import com.zenfulcode.commercify.payment.domain.service.PaymentDomainService;
import com.zenfulcode.commercify.payment.domain.service.PaymentProviderFactory;
import com.zenfulcode.commercify.payment.domain.service.PaymentProviderService;
import com.zenfulcode.commercify.payment.domain.valueobject.PaymentProviderResponse;
import com.zenfulcode.commercify.payment.domain.valueobject.PaymentStatus;
import com.zenfulcode.commercify.payment.domain.valueobject.TransactionId;
import com.zenfulcode.commercify.payment.domain.valueobject.webhook.WebhookPayload;
import com.zenfulcode.commercify.payment.infrastructure.webhook.WebhookHandler;
import com.zenfulcode.commercify.shared.domain.model.Money;
import com.zenfulcode.commercify.shared.domain.service.DefaultDomainEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PaymentApplicationService {
    private final PaymentDomainService paymentDomainService;
    private final PaymentProviderFactory providerFactory;
    private final DefaultDomainEventPublisher eventPublisher;
    private final WebhookHandler webhookHandler;

    @Transactional
    public InitializedPayment initiatePayment(InitiatePaymentCommand command) {
        // Get the appropriate provider service
        PaymentProviderService providerService = providerFactory.getProvider(command.provider());

        // Validate provider-specific request
        providerService.validateRequest(command.providerRequest());

        // Create domain payment entity
        Payment payment = paymentDomainService.createPayment(
                command.order(),
                command.paymentMethod(),
                command.provider()
        );

        // Initiate payment with provider
        PaymentProviderResponse providerResponse = providerService.initiatePayment(
                payment,
                command.order().getId(),
                command.providerRequest()
        );

        // Update payment with provider reference
        paymentDomainService.updateProviderReference(payment, providerResponse.providerReference());

        // Publish events
        eventPublisher.publish(payment.getDomainEvents());

        // Return response
        return new InitializedPayment(
                payment.getId(),
                providerResponse.redirectUrl(),
                providerResponse.additionalData()
        );
    }

    @Transactional
    public void handlePaymentCallback(PaymentProvider provider, WebhookPayload payload) {
        Payment payment = paymentDomainService.getPaymentByProviderReference(payload.getPaymentReference());
        webhookHandler.handleWebhook(provider, payload, payment);

        eventPublisher.publish(payment.getDomainEvents());
    }

    // TODO: Make sure the capture currency is the same as the payment currency
    @Transactional
    public CapturedPayment capturePayment(CapturePaymentCommand command) {
        Payment payment = paymentDomainService.getPaymentByOrderId(command.orderId());

        Money captureAmount = command.captureAmount() == null ? payment.getAmount() : command.captureAmount();

        paymentDomainService.capturePayment(payment, TransactionId.generate(), captureAmount);

        // Publish events
        eventPublisher.publish(payment.getDomainEvents());

        // This is going to be used for partial captures, which is not implemented yet
        BigDecimal remainingAmount = payment.getAmount().subtract(captureAmount).getAmount();
        CaptureAmount captureAmountDto = new CaptureAmount(captureAmount.getAmount(), remainingAmount, captureAmount.getCurrency());
        boolean isFullyCaptured = payment.getStatus() == PaymentStatus.CAPTURED;

        return new CapturedPayment(payment.getTransactionId(), captureAmountDto, isFullyCaptured);
    }

    public PaymentProvider getPaymentProvider(String provider) {
        try {
            return PaymentProvider.valueOf(provider.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new PaymentProviderNotFoundException(provider);
        }
    }
}
