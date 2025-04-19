package com.zenfulcode.commercify.shared.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "domain_events")
@NoArgsConstructor
public class StoredEvent {
    @Id
    @Column(name = "event_id", nullable = false)
    private String eventId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Lob
    @Column(name = "event_data", nullable = false)
    private String eventData;

    @Column(name = "occurred_on", nullable = false)
    private Instant occurredOn;

    @Column(name = "aggregate_id")
    private String aggregateId;

    @Column(name = "aggregate_type")
    private String aggregateType;

    public StoredEvent(
            String eventId,
            String eventType,
            String eventData,
            Instant occurredOn,
            String aggregateId,
            String aggregateType) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.eventData = eventData;
        this.occurredOn = occurredOn;
        this.aggregateId = aggregateId;
        this.aggregateType = aggregateType;
    }
}