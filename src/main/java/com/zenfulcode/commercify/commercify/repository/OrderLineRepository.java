package com.zenfulcode.commercify.commercify.repository;

import com.zenfulcode.commercify.commercify.OrderStatus;
import com.zenfulcode.commercify.commercify.entity.OrderEntity;
import com.zenfulcode.commercify.commercify.entity.OrderLineEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface OrderLineRepository extends JpaRepository<OrderLineEntity, Long> {
    Set<OrderLineEntity> findByOrder(OrderEntity order);

    void deleteOrderLinesByOrder(OrderEntity order);

    @Query("""
            SELECT DISTINCT o FROM OrderEntity o
            JOIN FETCH o.orderLines ol
            WHERE ol.productId = :productId
            AND o.status IN :activeStatuses
            ORDER BY o.createdAt DESC
            """)
    Set<OrderEntity> findActiveOrdersForProduct(
            @Param("productId") Long productId,
            @Param("activeStatuses") List<OrderStatus> activeStatuses
    );

    @Query("""
            SELECT DISTINCT o FROM OrderEntity o
            JOIN FETCH o.orderLines ol
            WHERE ol.variantId = :variantId
            AND o.status IN :activeStatuses
            ORDER BY o.createdAt DESC
            """)
    Set<OrderEntity> findActiveOrdersForVariant(Long id, List<OrderStatus> pending);
}