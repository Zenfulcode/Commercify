package com.zenfulcode.commercify.order.infrastructure.persistence;

import com.zenfulcode.commercify.order.domain.model.Order;
import com.zenfulcode.commercify.order.domain.repository.OrderRepository;
import com.zenfulcode.commercify.order.domain.valueobject.OrderId;
import com.zenfulcode.commercify.user.domain.valueobject.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JpaOrderRepository implements OrderRepository {
    private final SpringDataJpaOrderRepository repository;

    @Override
    public Order save(Order order) {
        return repository.save(order);
    }

    @Override
    public Optional<Order> findById(OrderId id) {
        return repository.findById(id);
    }

    @Override
    public Page<Order> findByUserId(UserId userId, PageRequest pageRequest) {
        return repository.findByUserId(userId, pageRequest);
    }

    @Override
    public Page<Order> findAll(PageRequest pageRequest) {
        return repository.findAll(pageRequest);
    }

    @Override
    public boolean existsByIdAndUserId(OrderId id, UserId userId) {
        return repository.existsByIdAndUserId(id, userId);
    }

    @Override
    public boolean existsByUserId(UserId userId) {
        return repository.findByUserId(userId, PageRequest.of(0, 1)).hasContent();
    }

    @Override
    public Optional<BigDecimal> calculateTotalRevenue(Instant startDate, Instant endDate) {
        return repository.calculateTotalRevenue(startDate, endDate);
    }

    @Override
    public int countOrders(Instant startDate, Instant endDate) {
        return repository.countOrders(startDate, endDate);
    }
}
