package com.zenfulcode.commercify.shared.domain.service;

import com.zenfulcode.commercify.shared.domain.event.DomainEvent;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class EventTypeResolver {
    private final Map<String, Class<? extends DomainEvent>> eventTypeMap = new ConcurrentHashMap<>();

    public Class<? extends DomainEvent> resolveEventClass(String eventType) throws ClassNotFoundException {
        return eventTypeMap.computeIfAbsent(eventType, this::loadEventClass);
    }

    @SuppressWarnings("unchecked")
    private Class<? extends DomainEvent> loadEventClass(String eventType) throws ClassNotFoundException {
        Class<?> loadedClass = Class.forName(eventType);
        if (!DomainEvent.class.isAssignableFrom(loadedClass)) {
            throw new IllegalArgumentException("Invalid event type: " + eventType);
        }
        return (Class<? extends DomainEvent>) loadedClass;
    }
}
