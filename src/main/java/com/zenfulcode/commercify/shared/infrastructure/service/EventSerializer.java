package com.zenfulcode.commercify.shared.infrastructure.service;

import com.fasterxml.jackson.databind.ObjectMapper;
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
                    extractAggregateId(event),
                    extractAggregateType(event)
            );
        } catch (Exception e) {
            throw new EventSerializationException("Failed to serialize event", e);
        }
    }

    public DomainEvent deserialize(StoredEvent storedEvent) {
        try {
            Class<?> eventClass = eventTypeResolver.resolveEventClass(storedEvent.getEventType());
            return (DomainEvent) objectMapper.readValue(storedEvent.getEventData(), eventClass);
        } catch (Exception e) {
            throw new EventDeserializationException(
                    "Failed to deserialize event: " + storedEvent.getEventType(), e);
        }
    }

    private String extractAggregateId(DomainEvent event) {
        return AggregateReferenceExtractor.extractAggregateId(event);
    }

    private String extractAggregateType(DomainEvent event) {
        return AggregateReferenceExtractor.extractAggregateType(event);
    }
}