package com.zenfulcode.commercify.shared.domain.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.Instant;
import java.util.UUID;

@Getter
public abstract class DomainEvent extends ApplicationEvent {
    private final String eventId;
    private final long occurredOn;
    private final String eventType;

    protected DomainEvent(Object source) {
        super(source);
        this.eventId = UUID.randomUUID().toString();
        this.occurredOn = getTimestamp();
        this.eventType = this.getClass().getSimpleName();
    }

    @Override
    @JsonIgnore
    public Object getSource() {
        return super.getSource();
    }

    public Instant getOccurredOn() {
        return Instant.ofEpochMilli(occurredOn);
    }
}