package com.zenfulcode.commercify.order.domain.service;

import com.zenfulcode.commercify.order.domain.model.OrderStatus;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Set;

@Component
public class OrderStateFlow {
    private final EnumMap<OrderStatus, Set<OrderStatus>> validTransitions;

    public OrderStateFlow() {
        validTransitions = new EnumMap<>(OrderStatus.class);

        validTransitions.put(OrderStatus.PENDING, Set.of(
                OrderStatus.PAID,
                OrderStatus.ABANDONED
        ));

        validTransitions.put(OrderStatus.ABANDONED, Set.of(
                OrderStatus.PENDING
        ));

        validTransitions.put(OrderStatus.PAID, Set.of(
                OrderStatus.SHIPPED,
                OrderStatus.COMPLETED,
                OrderStatus.CANCELLED
        ));

        validTransitions.put(OrderStatus.SHIPPED, Set.of(
                OrderStatus.COMPLETED
        ));

        validTransitions.put(OrderStatus.COMPLETED, Set.of(
                OrderStatus.REFUNDED
        ));

        // Terminal states
        validTransitions.put(OrderStatus.CANCELLED, Set.of());
        validTransitions.put(OrderStatus.REFUNDED, Set.of());
    }

    public boolean canTransition(OrderStatus currentState, OrderStatus newState) {
        return validTransitions.get(currentState).contains(newState);
    }

    public Set<OrderStatus> getValidTransitions(OrderStatus currentState) {
        return validTransitions.get(currentState);
    }

    public boolean isTerminalState(OrderStatus state) {
        return validTransitions.get(state).isEmpty();
    }
}
