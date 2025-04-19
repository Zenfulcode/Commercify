package com.zenfulcode.commercify.payment.infrastructure.persistence;

import com.zenfulcode.commercify.payment.domain.model.PaymentProvider;
import com.zenfulcode.commercify.payment.domain.model.WebhookConfig;
import com.zenfulcode.commercify.payment.domain.repository.WebhookConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JpaWebhookConfigRepository implements WebhookConfigRepository {
    private final SpringDataJpaWebhookConfigRepository repository;

    @Override
    public WebhookConfig save(WebhookConfig config) {
        return repository.save(config);
    }

    @Override
    public Optional<WebhookConfig> findByProvider(PaymentProvider provider) {
        return repository.findWebhookConfigByProvider(provider);
    }
}
