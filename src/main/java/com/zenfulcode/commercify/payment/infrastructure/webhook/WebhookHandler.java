package com.zenfulcode.commercify.payment.infrastructure.webhook;

import com.zenfulcode.commercify.payment.domain.model.PaymentProvider;
import com.zenfulcode.commercify.payment.domain.service.PaymentProviderFactory;
import com.zenfulcode.commercify.payment.domain.service.PaymentProviderService;
import com.zenfulcode.commercify.payment.domain.valueobject.webhook.WebhookPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WebhookHandler {
    private final PaymentProviderFactory providerFactory;

    public void handleWebhook(PaymentProvider provider, WebhookPayload payload) {
        PaymentProviderService service = providerFactory.getProvider(provider);
        service.handleCallback(payload);
    }
}
