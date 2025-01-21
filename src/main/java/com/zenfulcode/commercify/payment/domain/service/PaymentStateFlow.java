package com.zenfulcode.commercify.payment.domain.service;

import com.zenfulcode.commercify.payment.domain.exception.InvalidPaymentStateException;
import com.zenfulcode.commercify.payment.domain.valueobject.PaymentStateMetadata;
import com.zenfulcode.commercify.payment.domain.valueobject.PaymentStatus;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Set;

@Component
public class PaymentStateFlow {
    private final EnumMap<PaymentStatus, Set<PaymentStatus>> validTransitions;
    private final EnumMap<PaymentStatus, Boolean> terminalStates;

    public PaymentStateFlow() {
        this.validTransitions = new EnumMap<>(PaymentStatus.class);
        this.terminalStates = new EnumMap<>(PaymentStatus.class);
        initializeStateTransitions();
    }

    /**
     * Initialize valid state transitions
     */
    private void initializeStateTransitions() {
        // Initial state -> PENDING
        validTransitions.put(PaymentStatus.PENDING, Set.of(
                PaymentStatus.FAILED,
                PaymentStatus.RESERVED,
                PaymentStatus.CANCELLED
        ));

        // RESERVED/PAID -> RESERVED or CANCELLED
        validTransitions.put(PaymentStatus.RESERVED, Set.of(
                PaymentStatus.CAPTURED,
                PaymentStatus.EXPIRED,
                PaymentStatus.FAILED,
                PaymentStatus.PARTIALLY_REFUNDED,
                PaymentStatus.REFUNDED
        ));

        // CAPTURED -> REFUNDED or PARTIALLY_REFUNDED
        validTransitions.put(PaymentStatus.CAPTURED, Set.of(
                PaymentStatus.REFUNDED,
                PaymentStatus.PARTIALLY_REFUNDED
        ));

        // FAILED -> Can retry (go back to PENDING) or CANCELLED
        validTransitions.put(PaymentStatus.FAILED, Set.of(
                PaymentStatus.PENDING,
                PaymentStatus.CANCELLED
        ));

        // PARTIALLY_REFUNDED -> REFUNDED
        validTransitions.put(PaymentStatus.PARTIALLY_REFUNDED, Set.of(
                PaymentStatus.REFUNDED
        ));

        // Terminal states have no further transitions
        validTransitions.put(PaymentStatus.REFUNDED, Set.of());
        validTransitions.put(PaymentStatus.CANCELLED, Set.of());
        validTransitions.put(PaymentStatus.EXPIRED, Set.of());

        // Mark terminal states
        terminalStates.put(PaymentStatus.REFUNDED, true);
        terminalStates.put(PaymentStatus.CANCELLED, true);
        terminalStates.put(PaymentStatus.EXPIRED, true);
        terminalStates.put(PaymentStatus.CAPTURED, false);
        terminalStates.put(PaymentStatus.PENDING, false);
        terminalStates.put(PaymentStatus.FAILED, false);
        terminalStates.put(PaymentStatus.PARTIALLY_REFUNDED, false);
    }

    /**
     * Check if a state transition is valid
     */
    public boolean canTransitionTo(PaymentStatus currentState, PaymentStatus targetState) {
        Set<PaymentStatus> allowedTransitions = validTransitions.get(currentState);
        return allowedTransitions != null && allowedTransitions.contains(targetState);
    }

    /**
     * Get valid next states for a given state
     */
    public Set<PaymentStatus> getValidTransitions(PaymentStatus currentState) {
        return validTransitions.getOrDefault(currentState, Set.of());
    }

    /**
     * Check if a state is terminal
     */
    public boolean isTerminalState(PaymentStatus state) {
        return terminalStates.getOrDefault(state, false);
    }

    /**
     * Validate state transition and throw exception if invalid
     */
    public void validateStateTransition(PaymentStatus currentState, PaymentStatus targetState) {
        if (!canTransitionTo(currentState, targetState)) {
            throw new InvalidPaymentStateException(
                    null, // PaymentId would be set by the calling service
                    currentState,
                    targetState,
                    "Invalid payment status transition"
            );
        }
    }

    /**
     * Get state transition diagram for documentation
     */
    public String getStateTransitionDiagram() {
        StringBuilder diagram = new StringBuilder();
        diagram.append("Payment State Transitions:\n");
        diagram.append("------------------------\n");

        validTransitions.forEach((state, transitions) -> {
            if (!transitions.isEmpty()) {
                diagram.append(state).append(" -> ");
                diagram.append(String.join(" | ", transitions.stream()
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