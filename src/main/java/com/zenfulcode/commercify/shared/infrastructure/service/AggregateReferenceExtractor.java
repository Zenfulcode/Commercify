package com.zenfulcode.commercify.shared.infrastructure.service;

import com.zenfulcode.commercify.shared.domain.event.DomainEvent;
import com.zenfulcode.commercify.shared.domain.valueobject.AggregateId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
public class AggregateReferenceExtractor {

    private static final List<String> CONVENTIONAL_ID_NAMES = List.of(
            "aggregateId",
            "entityId",
            "orderId",
            "paymentId",
            "productId",
            "userId"
    );

    private static final List<String> EVENT_SUFFIXES = List.of(
            "Event",
            "Created",
            "Updated",
            "Deleted",
            "Changed",
            "Cancelled",
            "Completed",
            "Started",
            "Finished",
            "Failed"
    );

    public static String extractAggregateId(DomainEvent event) {
        // First try fields annotated with @AggregateId
        Optional<Field> annotatedField = findAnnotatedIdField(event);
        if (annotatedField.isPresent()) {
            return extractFieldValue(annotatedField.get(), event);
        }

        // Then try conventional ID field names
        Optional<Field> conventionalField = findConventionalIdField(event);
        if (conventionalField.isPresent()) {
            return extractFieldValue(conventionalField.get(), event);
        }

        // If no ID field is found, log a warning and use event ID
        log.warn("No aggregate ID field found for event: {}", event.getClass().getSimpleName());
        return event.getEventId();
    }

    public static String extractAggregateType(DomainEvent event) {
        // Remove common event-related suffixes
        String aggregateType = event.getClass().getSimpleName();
        for (String suffix : EVENT_SUFFIXES) {
            if (aggregateType.endsWith(suffix)) {
                aggregateType = aggregateType.substring(0,
                        aggregateType.length() - suffix.length());
            }
        }

        return aggregateType;
    }

    private static Optional<Field> findAnnotatedIdField(DomainEvent event) {
        List<Field> allFields = getAllFields(event.getClass());
        return allFields.stream()
                .filter(field -> field.isAnnotationPresent(AggregateId.class))
                .findFirst();
    }

    private static Optional<Field> findConventionalIdField(DomainEvent event) {
        List<Field> allFields = getAllFields(event.getClass());
        return allFields.stream()
                .filter(field -> CONVENTIONAL_ID_NAMES.stream()
                        .anyMatch(name -> field.getName().equalsIgnoreCase(name)))
                .findFirst();
    }

    private static List<Field> getAllFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        Class<?> currentClass = type;

        while (currentClass != null && currentClass != Object.class) {
            fields.addAll(List.of(currentClass.getDeclaredFields()));
            currentClass = currentClass.getSuperclass();
        }

        return fields;
    }

    private static String extractFieldValue(Field field, DomainEvent event) {
        try {
            ReflectionUtils.makeAccessible(field);
            Object value = field.get(event);
            return value != null ? value.toString() : null;
        } catch (IllegalAccessException e) {
            log.error("Could not access field: {} in event: {}",
                    field.getName(), event.getClass().getSimpleName(), e);
            return null;
        }
    }
}