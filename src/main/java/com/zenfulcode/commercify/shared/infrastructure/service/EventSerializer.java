package com.zenfulcode.commercify.shared.infrastructure.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zenfulcode.commercify.shared.domain.event.AggregateReference;
import com.zenfulcode.commercify.shared.domain.event.DomainEvent;
import com.zenfulcode.commercify.shared.domain.exception.EventDeserializationException;
import com.zenfulcode.commercify.shared.domain.exception.EventSerializationException;
import com.zenfulcode.commercify.shared.domain.model.StoredEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventSerializer {
    private final ObjectMapper objectMapper;
    private final EventTypeResolver eventTypeResolver;

    public StoredEvent serialize(DomainEvent event) {
        try {
            String eventData = objectMapper.writeValueAsString(event);
            return new StoredEvent(
                    event.getEventId(),
                    event.getClass().getName(),
                    eventData,
                    event.getOccurredOn(),
                    getAggregateId(event),
                    getAggregateType(event)
            );
        } catch (JsonProcessingException e) {
            throw new EventSerializationException("Failed to serialize event", e);
        }
    }

    public DomainEvent deserialize(StoredEvent storedEvent) {
        try {
            Class<?> eventClass = eventTypeResolver.resolveEventClass(storedEvent.getEventType());
            return (DomainEvent) objectMapper.readValue(
                    storedEvent.getEventData(),
                    eventClass
            );
        } catch (Exception e) {
            throw new EventDeserializationException(
                    "Failed to deserialize event: " + storedEvent.getEventType(),
                    e
            );
        }
    }

    private String getAggregateId(DomainEvent event) {
        return AggregateReference.extractId(event);
    }

    private String getAggregateType(DomainEvent event) {
        return AggregateReference.extractType(event);
    }
}