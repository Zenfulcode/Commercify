package com.zenfulcode.commercify.payment.application.service;

import com.zenfulcode.commercify.api.payment.mapper.PaymentDtoMapper;
import com.zenfulcode.commercify.payment.domain.model.PaymentProvider;
import com.zenfulcode.commercify.payment.domain.service.PaymentProviderFactory;
import com.zenfulcode.commercify.payment.domain.service.provider.MobilepayProviderService;
import com.zenfulcode.commercify.payment.domain.valueobject.WebhookRequest;
import com.zenfulcode.commercify.payment.domain.valueobject.webhook.WebhookPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MobilepayWebhookService {
    private final PaymentProviderFactory providerFactory;
    private final PaymentDtoMapper mapper;

    @Transactional
    public WebhookPayload authenticate(PaymentProvider provider, WebhookRequest request) {
        MobilepayProviderService paymentProvider = (MobilepayProviderService) providerFactory.getProvider(provider);

        String contentSha256 = request.headers().get("x-ms-content-sha256");
        String authorization = request.headers().get("authorization");
        String date = request.headers().get("x-ms-date");

        paymentProvider.authenticateWebhook(date, contentSha256, authorization, request.body());
        return mapper.toWebhookPayload(request);
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