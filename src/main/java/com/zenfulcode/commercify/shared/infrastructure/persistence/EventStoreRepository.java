package com.zenfulcode.commercify.shared.infrastructure.persistence;

import com.zenfulcode.commercify.shared.domain.model.StoredEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface EventStoreRepository extends JpaRepository<StoredEvent, String> {

    List<StoredEvent> findByAggregateIdAndAggregateType(String aggregateId, String aggregateType);

    @Query("SELECT e FROM StoredEvent e WHERE e.occurredOn >= :since ORDER BY e.occurredOn ASC")
    List<StoredEvent> findEventsSince(@Param("since") Instant since);

    @Query("SELECT e FROM StoredEvent e WHERE e.eventType = :eventType ORDER BY e.occurredOn ASC")
    List<StoredEvent> findByEventType(@Param("eventType") String eventType);

    @Query("SELECT e FROM StoredEvent e WHERE e.aggregateType = :aggregateType ORDER BY e.occurredOn ASC")
    Page<StoredEvent> findByAggregateType(
            @Param("aggregateType") String aggregateType,
            Pageable pageable
    );

    @Query("""
        SELECT e FROM StoredEvent e
        WHERE e.aggregateId = :aggregateId
        AND e.aggregateType = :aggregateType
        AND e.occurredOn >= :since
        ORDER BY e.occurredOn ASC
        """)
    List<StoredEvent> findByAggregateIdAndTypeSince(
            @Param("aggregateId") String aggregateId,
            @Param("aggregateType") String aggregateType,
            @Param("since") Instant since
    );

    @Query("""
        SELECT COUNT(e) > 0 FROM StoredEvent e
        WHERE e.aggregateId = :aggregateId
        AND e.aggregateType = :aggregateType
        AND e.eventType = :eventType
        """)
    boolean hasEventType(
            @Param("aggregateId") String aggregateId,
            @Param("aggregateType") String aggregateType,
            @Param("eventType") String eventType
    );
}
