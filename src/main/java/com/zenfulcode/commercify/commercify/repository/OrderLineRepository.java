package com.zenfulcode.commercify.commercify.repository;

import com.zenfulcode.commercify.commercify.OrderStatus;
import com.zenfulcode.commercify.commercify.dto.ActiveOrderDTO;
import com.zenfulcode.commercify.commercify.entity.OrderEntity;
import com.zenfulcode.commercify.commercify.entity.OrderLineEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface OrderLineRepository extends JpaRepository<OrderLineEntity, Long> {
    List<OrderLineEntity> findByOrder(OrderEntity order);

    void deleteOrderLinesByOrder(OrderEntity order);

    List<ActiveOrderDTO> findActiveOrdersForProduct(
            @Param("productId") Long productId,
            @Param("activeStatuses") Set<OrderStatus> activeStatuses,
            Pageable pageable
    );
}