package com.zenfulcode.commercify.commercify.repository;

import com.zenfulcode.commercify.commercify.OrderStatus;
import com.zenfulcode.commercify.commercify.entity.OrderEntity;
import com.zenfulcode.commercify.commercify.entity.OrderLineEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Set;

@Repository
public interface OrderLineRepository extends JpaRepository<OrderLineEntity, Integer> {
    @Query("SELECT DISTINCT ol.order FROM OrderLineEntity ol " +
            "WHERE ol.productId = :productId " +
            "AND ol.order.status IN :statuses")
    Set<OrderEntity> findActiveOrdersForProduct(
            @Param("productId") Integer productId,
            @Param("statuses") Collection<OrderStatus> statuses
    );

    @Query("SELECT DISTINCT ol.order FROM OrderLineEntity ol " +
            "JOIN ol.productVariant v " +
            "WHERE v.id = :variantId " +
            "AND ol.order.status IN :statuses")
    Set<OrderEntity> findActiveOrdersForVariant(
            @Param("variantId") Integer variantId,
            @Param("statuses") Collection<OrderStatus> statuses
    );
}