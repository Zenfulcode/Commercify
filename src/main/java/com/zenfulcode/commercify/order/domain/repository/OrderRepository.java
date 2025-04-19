package com.zenfulcode.commercify.order.domain.repository;

import com.zenfulcode.commercify.order.domain.model.Order;
import com.zenfulcode.commercify.order.domain.valueobject.OrderId;
import com.zenfulcode.commercify.user.domain.valueobject.UserId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

public interface OrderRepository {
    Order save(Order order);

    Optional<Order> findById(OrderId id);

    Page<Order> findByUserId(UserId userId, PageRequest pageRequest);

    Page<Order> findAll(PageRequest pageRequest);

    boolean existsByIdAndUserId(OrderId id, UserId userId);

    boolean existsByUserId(UserId userId);

    Optional<BigDecimal> calculateTotalRevenue(Instant startDate, Instant endDate);

    int countOrders(Instant startDate, Instant endDate);
}