package com.zenfulcode.commercify.shared.domain.service;

import com.zenfulcode.commercify.shared.domain.event.DomainEvent;
import com.zenfulcode.commercify.shared.domain.event.DomainEventHandler;
import com.zenfulcode.commercify.shared.domain.event.DomainEventPublisher;
import com.zenfulcode.commercify.shared.domain.event.DomainEventStore;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DefaultDomainEventPublisher implements DomainEventPublisher {
    private final ApplicationEventPublisher applicationEventPublisher;
    @Qualifier("enhancedEventStore")
    private final DomainEventStore eventStore;
    private final List<DomainEventHandler<?>> eventHandlers;

    @Override
    public void publish(DomainEvent event) {
        // Store event
        eventStore.store(event);

        // Publish to Spring's event system
        applicationEventPublisher.publishEvent(event);

        // Handle event
        handleEvent(event);
    }

    @Override
    public void publish(List<DomainEvent> events) {
        events.forEach(this::publish);
    }

    private void handleEvent(DomainEvent event) {
        eventHandlers.stream()
                .filter(handler -> handler.canHandle(event))
                .forEach(handler -> handler.handle(event));
    }
}
