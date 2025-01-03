package com.zenfulcode.commercify.shared.domain.event;

import java.util.List;

public interface DomainEventPublisher {
    void publish(DomainEvent event);
    void publish(List<DomainEvent> events);
}
