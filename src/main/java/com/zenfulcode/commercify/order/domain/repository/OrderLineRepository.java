package com.zenfulcode.commercify.order.domain.repository;

import com.zenfulcode.commercify.order.domain.model.Order;
import com.zenfulcode.commercify.order.domain.model.OrderLine;
import com.zenfulcode.commercify.order.domain.model.OrderStatus;
import com.zenfulcode.commercify.order.domain.valueobject.OrderId;
import com.zenfulcode.commercify.product.domain.valueobject.ProductId;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface OrderLineRepository {
    OrderLine save(OrderLine orderLine);

    List<OrderLine> findByOrderId(OrderId orderId);

    Set<Order> findActiveOrdersForProduct(
            ProductId productId,
            Collection<OrderStatus> statuses
    );

    Set<Order> findActiveOrdersForVariant(
            Long variantId,
            Collection<OrderStatus> statuses
    );

    boolean hasActiveOrders(
            ProductId productId
    );

    boolean hasActiveOrdersForVariant(
            ProductId variantId
    );
}
