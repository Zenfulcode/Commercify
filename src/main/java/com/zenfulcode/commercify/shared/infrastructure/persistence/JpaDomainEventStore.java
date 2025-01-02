package com.zenfulcode.commercify.shared.infrastructure.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zenfulcode.commercify.shared.domain.event.DomainEvent;
import com.zenfulcode.commercify.shared.domain.event.DomainEventStore;
import com.zenfulcode.commercify.shared.domain.exception.EventDeserializationException;
import com.zenfulcode.commercify.shared.domain.exception.EventSerializationException;
import com.zenfulcode.commercify.shared.domain.model.StoredEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class JpaDomainEventStore implements DomainEventStore {
    private final EventStoreRepository repository;
    private final ObjectMapper objectMapper;

    @Override
    public void store(DomainEvent event) {
        StoredEvent storedEvent = new StoredEvent(
                event.getEventId(),
                event.getEventType(),
                serializeEvent(event),
                event.getOccurredOn()
        );
        repository.save(storedEvent);
    }

    @Override
    public List<DomainEvent> getEvents(String aggregateId, String aggregateType) {
        return repository.findByAggregateIdAndAggregateType(aggregateId, aggregateType)
                .stream()
                .map(this::deserializeEvent)
                .collect(Collectors.toList());
    }

    private String serializeEvent(DomainEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new EventSerializationException("Failed to serialize event", e);
        }
    }

    private DomainEvent deserializeEvent(StoredEvent storedEvent) {
        try {
            Class<?> eventClass = Class.forName(storedEvent.getEventType());
            return (DomainEvent) objectMapper.readValue(
                    storedEvent.getEventData(),
                    eventClass
            );
        } catch (Exception e) {
            throw new EventDeserializationException("Failed to deserialize event", e);
        }
    }
}
