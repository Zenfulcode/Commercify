package com.zenfulcode.commercify.payment.domain.service;

import com.zenfulcode.commercify.payment.domain.valueobject.PaymentStateMetadata;
import com.zenfulcode.commercify.payment.domain.valueobject.PaymentStatus;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Set;

@Component
public class PaymentStateFlow {
    private final EnumMap<PaymentStatus, Set<PaymentStatus>> validTransitions;

    public PaymentStateFlow() {
        this.validTransitions = new EnumMap<>(PaymentStatus.class);

        // Initial state -> PENDING
        validTransitions.put(PaymentStatus.PENDING, Set.of(
                PaymentStatus.FAILED,
                PaymentStatus.RESERVED,
                PaymentStatus.TERMINATED
        ));

        validTransitions.put(PaymentStatus.RESERVED, Set.of(
                PaymentStatus.CAPTURED,
                PaymentStatus.CANCELLED,
                PaymentStatus.EXPIRED,
                PaymentStatus.FAILED,
                PaymentStatus.PARTIALLY_REFUNDED,
                PaymentStatus.REFUNDED
        ));

        // TODO: Unsure about this one
        // CAPTURED -> REFUNDED or PARTIALLY_REFUNDED
        validTransitions.put(PaymentStatus.CAPTURED, Set.of(
                PaymentStatus.REFUNDED,
                PaymentStatus.PARTIALLY_REFUNDED
        ));

        // PARTIALLY_REFUNDED -> REFUNDED
        validTransitions.put(PaymentStatus.PARTIALLY_REFUNDED, Set.of(
                PaymentStatus.REFUNDED
        ));

        // Terminal states have no further transitions
        validTransitions.put(PaymentStatus.REFUNDED, Set.of());
        validTransitions.put(PaymentStatus.CANCELLED, Set.of());
        validTransitions.put(PaymentStatus.EXPIRED, Set.of());
        validTransitions.put(PaymentStatus.TERMINATED, Set.of());
        validTransitions.put(PaymentStatus.FAILED, Set.of());
    }


    /**
     * Check if a state transition is valid
     */
    public boolean canTransitionTo(PaymentStatus currentState, PaymentStatus targetState) {
        final PaymentStateMetadata metadata = getStateMetadata(currentState);
        return metadata.canTransitionTo(targetState);
    }

    /**
     * Get valid next states for a given state
     */
    private Set<PaymentStatus> getValidTransitions(PaymentStatus currentState) {
        return validTransitions.getOrDefault(currentState, Set.of());
    }

    /**
     * Check if a state is terminal
     */
    public boolean isTerminalState(PaymentStatus state) {
        return validTransitions.get(state).isEmpty();
    }

    /**
     * Get state transition diagram for documentation
     */
    public String getStateTransitionDiagram() {
        StringBuilder diagram = new StringBuilder();
        diagram.append("Payment State Transitions:\n");
        diagram.append("------------------------\n");

        validTransitions.forEach((state, transitions) -> {
            PaymentStateMetadata metadata = getStateMetadata(state);

            if (metadata.hasTransitions()) {
                diagram.append(state).append(" -> ");
                diagram.append(String.join(" | ", metadata.validTransitions().stream()
                        .map(PaymentStatus::name)
                        .toList()));
                diagram.append("\n");
            } else {
                diagram.append(state).append(" (Terminal State)\n");
            }
        });

        return diagram.toString();
    }

    /**
     * Get state metadata including whether it's terminal and valid transitions
     */
    public PaymentStateMetadata getStateMetadata(PaymentStatus state) {
        return new PaymentStateMetadata(
                state,
                isTerminalState(state),
                getValidTransitions(state)
        );
    }
}