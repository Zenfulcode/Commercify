package com.zenfulcode.commercify.shared.infrastructure.persistence;

import com.zenfulcode.commercify.shared.domain.event.DomainEvent;
import com.zenfulcode.commercify.shared.domain.event.DomainEventStore;
import com.zenfulcode.commercify.shared.domain.model.StoredEvent;
import com.zenfulcode.commercify.shared.infrastructure.service.EventSerializer;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JpaDomainEventStore implements DomainEventStore {
    private final EventStoreRepository repository;
    private final EventSerializer eventSerializer;

    @Async
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleDomainEvent(DomainEvent event) {
        store(event);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void store(DomainEvent event) {
        StoredEvent storedEvent = eventSerializer.serialize(event);
        repository.save(storedEvent);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DomainEvent> getEvents(String aggregateId, String aggregateType) {
        return repository.findByAggregateIdAndAggregateType(aggregateId, aggregateType)
                .stream()
                .map(eventSerializer::deserialize)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DomainEvent> getEventsSince(Instant since) {
        return repository.findEventsSince(since)
                .stream()
                .map(eventSerializer::deserialize)
                .toList();
    }
}
