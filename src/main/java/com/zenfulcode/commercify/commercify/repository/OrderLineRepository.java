package com.zenfulcode.commercify.commercify.repository;

import com.zenfulcode.commercify.commercify.OrderStatus;
import com.zenfulcode.commercify.commercify.entity.OrderEntity;
import com.zenfulcode.commercify.commercify.entity.OrderLineEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Repository
public interface OrderLineRepository extends JpaRepository<OrderLineEntity, Long> {
    @Query("SELECT DISTINCT ol.order FROM OrderLineEntity ol " +
            "WHERE ol.productId = :productId " +
            "AND ol.order.status IN :statuses")
    Set<OrderEntity> findActiveOrdersForProduct(
            @Param("productId") Long productId,
            @Param("statuses") Collection<OrderStatus> statuses
    );

    @Query("SELECT DISTINCT ol.order FROM OrderLineEntity ol " +
            "JOIN ol.productVariant v " +
            "WHERE v.id = :variantId " +
            "AND ol.order.status IN :statuses")
    Set<OrderEntity> findActiveOrdersForVariant(
            @Param("variantId") Long variantId,
            @Param("statuses") Collection<OrderStatus> statuses
    );

    @Query("SELECT ol FROM OrderLineEntity ol " +
            "WHERE ol.order.id = :orderId")
    List<OrderLineEntity> findByOrderId(@Param("orderId") Long orderId);

    @Query("SELECT COUNT(ol) > 0 FROM OrderLineEntity ol " +
            "WHERE ol.productId = :productId " +
            "AND ol.order.status IN :statuses")
    boolean hasActiveOrders(
            @Param("productId") Long productId,
            @Param("statuses") Collection<OrderStatus> statuses
    );

    @Query("SELECT COUNT(ol) > 0 FROM OrderLineEntity ol " +
            "JOIN ol.productVariant v " +
            "WHERE v.id = :variantId " +
            "AND ol.order.status IN :statuses")
    boolean hasActiveOrdersForVariant(
            @Param("variantId") Long variantId,
            @Param("statuses") Collection<OrderStatus> statuses
    );
}