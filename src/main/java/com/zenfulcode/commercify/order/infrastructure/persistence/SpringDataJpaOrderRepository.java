package com.zenfulcode.commercify.order.infrastructure.persistence;

import com.zenfulcode.commercify.order.domain.model.Order;
import com.zenfulcode.commercify.order.domain.valueobject.OrderId;
import com.zenfulcode.commercify.user.domain.valueobject.UserId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

@Repository
interface SpringDataJpaOrderRepository extends JpaRepository<Order, OrderId> {
    Page<Order> findByUserId(UserId userId, Pageable pageable);

    boolean existsByIdAndUserId(OrderId id, UserId userId);

    @Query("""
                SELECT SUM(o.subtotal.amount)
                FROM Order o
                WHERE o.status = 'COMPLETED'
                  AND o.createdAt BETWEEN :startDate AND :endDate
            """)
    Optional<BigDecimal> calculateTotalRevenue(
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );

    @Query("""
                SELECT COUNT(o)
                FROM Order o
                WHERE o.createdAt BETWEEN :startDate AND :endDate
            """)
    int countOrders(
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );
}