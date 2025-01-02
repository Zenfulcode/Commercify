package com.zenfulcode.commercify.shared.infrastructure.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zenfulcode.commercify.shared.domain.event.DomainEvent;
import com.zenfulcode.commercify.shared.domain.event.DomainEventStore;
import com.zenfulcode.commercify.shared.domain.model.StoredEvent;
import com.zenfulcode.commercify.shared.domain.service.EventSerializer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnhancedEventStore implements DomainEventStore {
    private final EventStoreRepository repository;
    private final ObjectMapper objectMapper;
    private final EventSerializer eventSerializer;

    @Override
    public void store(DomainEvent event) {
        StoredEvent storedEvent = eventSerializer.serialize(event);
        repository.save(storedEvent);
    }

    @Override
    public List<DomainEvent> getEvents(String aggregateId, String aggregateType) {
        return repository.findByAggregateIdAndAggregateType(aggregateId, aggregateType)
                .stream()
                .map(eventSerializer::deserialize)
                .collect(Collectors.toList());
    }

    public List<DomainEvent> getEventsSince(Instant since) {
        return repository.findEventsSince(since)
                .stream()
                .map(eventSerializer::deserialize)
                .collect(Collectors.toList());
    }

    public <T extends DomainEvent> List<T> getEventsByType(Class<T> eventType) {
        return repository.findByEventType(eventType.getName())
                .stream()
                .map(event -> (T) eventSerializer.deserialize(event))
                .collect(Collectors.toList());
    }

    public Page<DomainEvent> getEventsByAggregateType(String aggregateType, Pageable pageable) {
        return repository.findByAggregateType(aggregateType, pageable)
                .map(eventSerializer::deserialize);
    }

    public boolean hasEventOccurred(String aggregateId, String aggregateType, String eventType) {
        return repository.hasEventType(aggregateId, aggregateType, eventType);
    }
}
