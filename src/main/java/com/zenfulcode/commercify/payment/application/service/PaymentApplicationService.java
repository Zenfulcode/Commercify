package com.zenfulcode.commercify.payment.application.service;

import com.zenfulcode.commercify.payment.application.command.InitiatePaymentCommand;
import com.zenfulcode.commercify.payment.application.dto.PaymentResponse;
import com.zenfulcode.commercify.payment.domain.model.Payment;
import com.zenfulcode.commercify.payment.domain.model.PaymentProvider;
import com.zenfulcode.commercify.payment.domain.service.PaymentDomainService;
import com.zenfulcode.commercify.payment.domain.service.PaymentProviderFactory;
import com.zenfulcode.commercify.payment.domain.service.PaymentProviderService;
import com.zenfulcode.commercify.payment.domain.valueobject.PaymentProviderResponse;
import com.zenfulcode.commercify.payment.domain.valueobject.webhook.WebhookPayload;
import com.zenfulcode.commercify.shared.domain.event.DomainEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentApplicationService {
    private final PaymentDomainService paymentDomainService;
    private final PaymentProviderFactory providerFactory;
    private final DomainEventPublisher eventPublisher;

    @Transactional
    public PaymentResponse initiatePayment(InitiatePaymentCommand command) {
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
        payment.updateProviderReference(providerResponse.providerReference());

        // Publish events
        eventPublisher.publish(payment.getDomainEvents());

        // Return response
        return new PaymentResponse(
                payment.getId(),
                providerResponse.redirectUrl(),
                providerResponse.additionalData()
        );
    }

    @Transactional
    public void handlePaymentCallback(PaymentProvider provider, WebhookPayload payload) {
        PaymentProviderService providerService = providerFactory.getProvider(provider);
        providerService.handleCallback(payload);
    }
}
