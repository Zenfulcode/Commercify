package com.zenfulcode.commercify.shared.domain.exception;

import lombok.Getter;

@Getter
public class EntityNotFoundException extends DomainException {
    private final String entityType;
    private final Object entityId;

    public EntityNotFoundException(String entityType, Object entityId) {
        super(String.format("%s with id %s not found", entityType, entityId));
        this.entityType = entityType;
        this.entityId = entityId;
    }
}