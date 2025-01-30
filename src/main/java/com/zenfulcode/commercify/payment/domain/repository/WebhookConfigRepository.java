package com.zenfulcode.commercify.payment.domain.repository;

import com.zenfulcode.commercify.payment.domain.model.PaymentProvider;
import com.zenfulcode.commercify.payment.domain.model.WebhookConfig;

import java.util.Optional;

public interface WebhookConfigRepository {
    WebhookConfig save(WebhookConfig config);

    Optional<WebhookConfig> findByProvider(PaymentProvider provider);
}
