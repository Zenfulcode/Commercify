package com.zenfulcode.commercify.shared.domain.service;

import com.zenfulcode.commercify.shared.domain.event.DomainEvent;
import com.zenfulcode.commercify.shared.domain.event.DomainEventPublisher;
import com.zenfulcode.commercify.shared.domain.event.DomainEventStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultDomainEventPublisher implements DomainEventPublisher {
    private final ApplicationEventPublisher eventPublisher;
    private final DomainEventStore eventStore;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void publish(DomainEvent event) {
        try {
            log.info("Publishing event: {}", event.getEventType());

            // Store the event
            eventStore.store(event);

            // Publish to Spring's event system
            eventPublisher.publishEvent(event);

            log.info("Successfully published event: {}", event.getEventType());
        } catch (Exception e) {
            log.error("Failed to publish event: {}", event.getEventType(), e);
            throw e;
        }
    }

    @Override
    public void publish(List<DomainEvent> events) {
        events.forEach(this::publish);
    }
}
