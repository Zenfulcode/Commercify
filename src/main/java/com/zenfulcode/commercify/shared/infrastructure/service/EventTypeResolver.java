package com.zenfulcode.commercify.shared.infrastructure.service;

import com.zenfulcode.commercify.shared.domain.event.DomainEvent;
import com.zenfulcode.commercify.shared.domain.exception.EventDeserializationException;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class EventTypeResolver {
    private final Map<String, Class<? extends DomainEvent>> eventTypeMap = new ConcurrentHashMap<>();

    public Class<? extends DomainEvent> resolveEventClass(String eventType) {
        return eventTypeMap.computeIfAbsent(eventType, type -> {
            try {
                return loadEventClass(type);
            } catch (ClassNotFoundException e) {
                throw new EventDeserializationException("Failed to load event class: " + type, e);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private Class<? extends DomainEvent> loadEventClass(String eventType) throws ClassNotFoundException {
        Class<?> loadedClass = Class.forName(eventType);
        if (!DomainEvent.class.isAssignableFrom(loadedClass)) {
            throw new IllegalArgumentException("Invalid event type: " + eventType);
        }
        return (Class<? extends DomainEvent>) loadedClass;
    }

    public void registerEventType(String eventType, Class<? extends DomainEvent> eventClass) {
        eventTypeMap.put(eventType, eventClass);
    }

    public boolean isRegistered(String eventType) {
        return eventTypeMap.containsKey(eventType);
    }

    public void clearRegistrations() {
        eventTypeMap.clear();
    }
}