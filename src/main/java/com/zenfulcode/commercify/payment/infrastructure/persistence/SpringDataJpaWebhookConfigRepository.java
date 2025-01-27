package com.zenfulcode.commercify.payment.infrastructure.persistence;

import com.zenfulcode.commercify.payment.domain.model.PaymentProvider;
import com.zenfulcode.commercify.payment.domain.model.WebhookConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataJpaWebhookConfigRepository extends JpaRepository<WebhookConfig, PaymentProvider> {
    Optional<WebhookConfig> findWebhookConfigByProvider(PaymentProvider provider);
}
