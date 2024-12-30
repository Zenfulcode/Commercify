package com.zenfulcode.commercify.component.flow;

import com.zenfulcode.commercify.domain.enums.OrderStatus;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Set;

@Component
public class OrderStateFlow {
    private final EnumMap<OrderStatus, Set<OrderStatus>> validTransitions;

    public OrderStateFlow() {
        validTransitions = new EnumMap<>(OrderStatus.class);

        // Initial state -> Confirmed or Cancelled
        validTransitions.put(OrderStatus.PENDING, Set.of(
                OrderStatus.CONFIRMED,
                OrderStatus.CANCELLED
        ));

        // Payment received -> Processing or Cancelled
        validTransitions.put(OrderStatus.CONFIRMED, Set.of(
                OrderStatus.SHIPPED,
                OrderStatus.CANCELLED
        ));

        // Shipped -> Completed or Returned
        validTransitions.put(OrderStatus.SHIPPED, Set.of(
                OrderStatus.COMPLETED,
                OrderStatus.RETURNED
        ));

        // Terminal states
        validTransitions.put(OrderStatus.COMPLETED, Set.of());
        validTransitions.put(OrderStatus.CANCELLED, Set.of());
        validTransitions.put(OrderStatus.RETURNED, Set.of());
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