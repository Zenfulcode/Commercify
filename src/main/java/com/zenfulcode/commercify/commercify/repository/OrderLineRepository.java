package com.zenfulcode.commercify.commercify.repository;

import com.zenfulcode.commercify.commercify.OrderStatus;
import com.zenfulcode.commercify.commercify.dto.ActiveOrderDTO;
import com.zenfulcode.commercify.commercify.entity.OrderEntity;
import com.zenfulcode.commercify.commercify.entity.OrderLineEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface OrderLineRepository extends JpaRepository<OrderLineEntity, Long> {
    List<OrderLineEntity> findByOrder(OrderEntity order);

    void deleteOrderLinesByOrder(OrderEntity order);

    @Query("""
                SELECT CASE WHEN COUNT(ol) > 0 THEN true ELSE false END
                FROM OrderLineEntity ol
                WHERE ol.productId = :productId
                AND ol.order.status IN :activeStatuses
            """)
    boolean existsActiveOrdersForProduct(
            @Param("productId") Long productId,
            @Param("activeStatuses") Set<OrderStatus> activeStatuses
    );

    @Query("""
                SELECT COUNT(ol)
                FROM OrderLineEntity ol
                WHERE ol.productId = :productId
                AND ol.order.status IN :activeStatuses
            """)
    long countActiveOrdersForProduct(
            @Param("productId") Long productId,
            @Param("activeStatuses") Set<OrderStatus> activeStatuses
    );

    @Query("""
                SELECT new com.zenfulcode.commercify.commercify.dto.ActiveOrderDTO(
                    ol.order.orderId,
                    ol.order.status,
                    ol.quantity,
                    ol.order.createdAt
                )
                FROM OrderLineEntity ol
                WHERE ol.productId = :productId
                AND ol.order.status IN :activeStatuses
                ORDER BY ol.order.createdAt DESC
            """)
    List<ActiveOrderDTO> findActiveOrdersForProduct(
            @Param("productId") Long productId,
            @Param("activeStatuses") Set<OrderStatus> activeStatuses,
            Pageable pageable
    );
}