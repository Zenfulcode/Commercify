package com.zenfulcode.commercify.shared.domain.event;

import com.zenfulcode.commercify.shared.domain.model.AggregateRoot;
import com.zenfulcode.commercify.shared.domain.valueobject.AggregateId;

import java.lang.reflect.Field;
import java.util.Arrays;

public class AggregateReference {

    public static String extractId(DomainEvent event) {
        // First try to find a field ending with "Id"
        String id = Arrays.stream(event.getClass().getDeclaredFields())
                .filter(field -> field.getName().toLowerCase().endsWith("id"))
                .findFirst()
                .map(field -> getFieldValue(field, event))
                .map(Object::toString)
                .orElse(null);

        if (id == null) {
            // Fallback to any field annotated with @AggregateId if exists
            id = Arrays.stream(event.getClass().getDeclaredFields())
                    .filter(field -> field.isAnnotationPresent(AggregateId.class))
                    .findFirst()
                    .map(field -> getFieldValue(field, event))
                    .map(Object::toString)
                    .orElse(event.getEventId()); // Use event ID as last resort
        }

        return id;
    }

    public static String extractType(DomainEvent event) {
        // Try to extract from class name (e.g., OrderCreatedEvent -> Order)
        String className = event.getClass().getSimpleName();
        if (className.endsWith("Event")) {
            return className.substring(0, className.length() - "Event".length());
        }

        // Fallback to any field that is an AggregateRoot
        return Arrays.stream(event.getClass().getDeclaredFields())
                .filter(field -> AggregateRoot.class.isAssignableFrom(field.getType()))
                .findFirst()
                .map(Field::getType)
                .map(Class::getSimpleName)
                .orElse("Unknown");
    }

    private static Object getFieldValue(Field field, Object target) {
        try {
            field.setAccessible(true);
            return field.get(target);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Could not access field: " + field.getName(), e);
        }
    }
}
