package com.zenfulcode.commercify.shared.domain.event;

public interface DomainEventHandler<T extends DomainEvent> {
    void handle(T event);
    boolean canHandle(DomainEvent event);
}

