package com.zenfulcode.commercify.commercify.flow;

import com.zenfulcode.commercify.commercify.PaymentStatus;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Set;

@Component
public class PaymentStateFlow {
    private final EnumMap<PaymentStatus, Set<PaymentStatus>> validTransitions;

    public PaymentStateFlow() {
        validTransitions = new EnumMap<>(PaymentStatus.class);

        // Initial state -> Paid, Failed, or Cancelled
        validTransitions.put(PaymentStatus.PENDING, Set.of(
                PaymentStatus.PAID,
                PaymentStatus.FAILED,
                PaymentStatus.CANCELLED,
                PaymentStatus.EXPIRED
        ));

        // Successful payment -> Refunded
        validTransitions.put(PaymentStatus.PAID, Set.of(
                PaymentStatus.REFUNDED
        ));

        // Terminal states
        validTransitions.put(PaymentStatus.FAILED, Set.of());
        validTransitions.put(PaymentStatus.CANCELLED, Set.of());
        validTransitions.put(PaymentStatus.REFUNDED, Set.of());
        validTransitions.put(PaymentStatus.EXPIRED, Set.of());
        validTransitions.put(PaymentStatus.TERMINATED, Set.of());
    }

    public boolean canTransition(PaymentStatus currentState, PaymentStatus newState) {
        return validTransitions.get(currentState).contains(newState);
    }

    public Set<PaymentStatus> getValidTransitions(PaymentStatus currentState) {
        return validTransitions.get(currentState);
    }

    public boolean isTerminalState(PaymentStatus state) {
        return validTransitions.get(state).isEmpty();
    }
}