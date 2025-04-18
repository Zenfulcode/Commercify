package com.zenfulcode.commercify.order.infrastructure.persistence;

import com.zenfulcode.commercify.order.domain.model.Order;
import com.zenfulcode.commercify.order.domain.model.OrderLine;
import com.zenfulcode.commercify.order.domain.model.OrderStatus;
import com.zenfulcode.commercify.order.domain.repository.OrderLineRepository;
import com.zenfulcode.commercify.order.domain.valueobject.OrderId;
import com.zenfulcode.commercify.product.domain.valueobject.ProductId;
import com.zenfulcode.commercify.product.domain.valueobject.VariantId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class JpaOrderLineRepository implements OrderLineRepository {

    private final SpringDataJpaOrderLineRepository repository;

    @Override
    public OrderLine save(OrderLine orderLine) {
        return repository.save(orderLine);
    }

    @Override
    public List<OrderLine> findByOrderId(OrderId orderId) {
        return repository.findByOrderId(orderId);
    }

    @Override
    public Set<Order> findActiveOrdersForProduct(
            ProductId productId, Collection<OrderStatus> statuses) {
        return repository.findActiveOrdersForProduct(productId, statuses);
    }

    @Override
    public Set<Order> findActiveOrdersForVariant(
            VariantId variantId, Collection<OrderStatus> statuses) {
        return repository.findActiveOrdersForVariant(variantId, statuses);
    }

    @Override
    public boolean hasActiveOrders(ProductId productId) {
        return repository.hasActiveOrders(productId);
    }

    @Override
    public boolean hasActiveOrdersForVariant(VariantId variantId) {
        return repository.hasActiveOrdersForVariant(variantId);
    }
}
