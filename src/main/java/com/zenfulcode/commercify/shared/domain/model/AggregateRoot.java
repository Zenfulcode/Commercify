package com.zenfulcode.commercify.shared.domain.model;

import com.zenfulcode.commercify.shared.domain.event.DomainEvent;
import org.springframework.data.annotation.Transient;

import java.util.ArrayList;
import java.util.List;

public abstract class AggregateRoot {
    @Transient
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    protected void registerEvent(DomainEvent event) {
        domainEvents.add(event);
    }

    public List<DomainEvent> getDomainEvents() {
        return new ArrayList<>(domainEvents);
    }

    public void clearDomainEvents() {
        domainEvents.clear();
    }
}
