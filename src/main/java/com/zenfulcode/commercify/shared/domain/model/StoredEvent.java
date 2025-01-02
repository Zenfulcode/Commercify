package com.zenfulcode.commercify.shared.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "domain_events")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoredEvent {
    @Id
    private String eventId;

    @Column(nullable = false)
    private String eventType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String eventData;

    @Column(nullable = false)
    private Instant occurredOn;

    @Column
    private String aggregateId;

    @Column
    private String aggregateType;

    public StoredEvent(String eventId, String eventType, String eventData, Instant occurredOn) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.eventData = eventData;
        this.occurredOn = occurredOn;
    }
}
