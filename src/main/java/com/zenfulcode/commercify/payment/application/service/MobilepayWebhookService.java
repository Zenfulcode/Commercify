package com.zenfulcode.commercify.payment.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zenfulcode.commercify.payment.domain.model.PaymentProvider;
import com.zenfulcode.commercify.payment.domain.service.PaymentProviderFactory;
import com.zenfulcode.commercify.payment.domain.service.PaymentProviderService;
import com.zenfulcode.commercify.payment.domain.service.provider.MobilepayProviderService;
import com.zenfulcode.commercify.payment.domain.valueobject.WebhookRequest;
import com.zenfulcode.commercify.payment.domain.valueobject.webhook.MobilepayWebhookPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MobilepayWebhookService {
    private final PaymentProviderFactory providerFactory;
    private final ObjectMapper objectMapper;

    @Transactional
    public void handleWebhook(PaymentProvider provider, WebhookRequest request) {
        PaymentProviderService service = providerFactory.getProvider(provider);

        // TODO: Refactor this, it's bad practice
        if (service instanceof MobilepayProviderService mobilePayService) {
            String contentSha256 = request.headers().get("Content-SHA256");
            String authorization = request.headers().get("Authorization");
            String date = request.headers().get("Date");

            mobilePayService.authenticateWebhook(date, contentSha256, authorization, request.body());
        }

        MobilepayWebhookPayload payload = objectMapper.convertValue(request.body(), MobilepayWebhookPayload.class);

        service.handleCallback(payload);
    }

    @Transactional
    public void registerWebhook(PaymentProvider provider, String callbackUrl) {
        providerFactory.getProvider(provider).registerWebhook(callbackUrl);
    }

    @Transactional
    public void deleteWebhook(PaymentProvider provider, String webhookId) {
        providerFactory.getProvider(provider).deleteWebhook(webhookId);
    }

    @Transactional(readOnly = true)
    public Object getWebhooks(PaymentProvider provider) {
        return providerFactory.getProvider(provider).getWebhooks();
    }
}