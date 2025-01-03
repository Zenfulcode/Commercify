package com.zenfulcode.commercify.order.infrastructure.persistence;

import com.zenfulcode.commercify.order.domain.model.Order;
import com.zenfulcode.commercify.order.domain.model.OrderLine;
import com.zenfulcode.commercify.order.domain.model.OrderStatus;
import com.zenfulcode.commercify.order.domain.valueobject.OrderId;
import com.zenfulcode.commercify.product.domain.valueobject.ProductId;
import com.zenfulcode.commercify.product.domain.valueobject.VariantId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Repository
interface SpringDataJpaOrderLineRepository extends JpaRepository<OrderLine, Long> {

    List<OrderLine> findByOrderId(OrderId orderId);

    @Query("""
            SELECT DISTINCT ol.order FROM OrderLine ol
            WHERE ol.productId = :productId
            AND ol.order.status IN :statuses
            """)
    Set<Order> findActiveOrdersForProduct(
            @Param("productId") ProductId productId,
            @Param("statuses") Collection<OrderStatus> statuses
    );

    @Query("""
            SELECT DISTINCT ol.order FROM OrderLine ol
            JOIN ol.productVariant v
            WHERE v.id = :variantId
            AND ol.order.status IN :statuses
            """)
    Set<Order> findActiveOrdersForVariant(
            @Param("variantId") VariantId variantId,
            @Param("statuses") Collection<OrderStatus> statuses
    );

    @Query("""
            SELECT COUNT(ol) > 0 FROM OrderLine ol
            WHERE ol.productId = :productId
            """)
    boolean hasActiveOrders(
            @Param("productId") ProductId productId
    );

    @Query("""
            SELECT COUNT(ol) > 0 FROM OrderLine ol
            JOIN ol.productVariant v
            WHERE v.id = :variantId
            """)
    boolean hasActiveOrdersForVariant(
            @Param("variantId") VariantId variantId
    );
}
