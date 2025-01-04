package com.zenfulcode.commercify.shared.domain.event;

public interface DomainEventHandler {
    void handle(DomainEvent event);
    boolean canHandle(DomainEvent event);
}

