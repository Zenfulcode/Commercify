package com.zenfulcode.commercify.order.infrastructure.persistence;

import com.zenfulcode.commercify.order.domain.model.Order;
import com.zenfulcode.commercify.order.domain.valueobject.OrderId;
import com.zenfulcode.commercify.user.domain.valueobject.UserId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface SpringDataJpaOrderRepository extends JpaRepository<Order, OrderId> {
    Page<Order> findByUserId(UserId userId, Pageable pageable);

    boolean existsByIdAndUserId(OrderId id, UserId userId);
}