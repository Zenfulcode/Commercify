package com.zenfulcode.commercify.commercify.repository;

import com.zenfulcode.commercify.commercify.entity.WebhookConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WebhookConfigRepository extends JpaRepository<WebhookConfigEntity, Long> {
    Optional<WebhookConfigEntity> findByProvider(String provider);
}