package com.zenfulcode.commercify.commercify.repository;

import com.zenfulcode.commercify.commercify.entity.OrderEntity;
import com.zenfulcode.commercify.commercify.entity.OrderLineEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderLineRepository extends JpaRepository<OrderLineEntity, Long> {
    List<OrderLineEntity> findByOrder(OrderEntity order);

    void deleteOrderLinesByOrder(OrderEntity order);
}