package com.zenfulcode.commercify.repository;

import com.zenfulcode.commercify.domain.enums.OrderStatus;
import com.zenfulcode.commercify.domain.model.Order;
import com.zenfulcode.commercify.domain.model.OrderLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Set;

@Repository
public interface OrderLineRepository extends JpaRepository<OrderLine, Long> {
    @Query("SELECT DISTINCT ol.order FROM OrderLine ol " +
            "WHERE ol.productId = :productId " +
            "AND ol.order.status IN :statuses")
    Set<Order> findActiveOrdersForProduct(
            @Param("productId") Long productId,
            @Param("statuses") Collection<OrderStatus> statuses
    );

    @Query("SELECT DISTINCT ol.order FROM OrderLine ol " +
            "JOIN ol.productVariant v " +
            "WHERE v.id = :variantId " +
            "AND ol.order.status IN :statuses")
    Set<Order> findActiveOrdersForVariant(
            @Param("variantId") Long variantId,
            @Param("statuses") Collection<OrderStatus> statuses
    );
}