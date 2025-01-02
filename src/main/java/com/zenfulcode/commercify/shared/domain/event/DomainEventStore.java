package com.zenfulcode.commercify.shared.domain.event;

import java.util.List;

public interface DomainEventStore {
    void store(DomainEvent event);
    List<DomainEvent> getEvents(String aggregateId, String aggregateType);
}
