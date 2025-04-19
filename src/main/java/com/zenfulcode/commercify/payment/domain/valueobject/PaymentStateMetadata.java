package com.zenfulcode.commercify.payment.domain.valueobject;

import java.util.Set;

/**
 * Record to hold state metadata
 */
public record PaymentStateMetadata(
        PaymentStatus state,
        boolean isTerminal,
        Set<PaymentStatus> validTransitions
) {
    public boolean canTransitionTo(PaymentStatus targetState) {
        return validTransitions.contains(targetState);
    }

    public boolean hasTransitions() {
        return !validTransitions.isEmpty();
    }
}