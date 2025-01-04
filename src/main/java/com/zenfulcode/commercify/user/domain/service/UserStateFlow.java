package com.zenfulcode.commercify.user.domain.service;

import com.zenfulcode.commercify.user.domain.model.UserStatus;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Set;

@Component
public class UserStateFlow {
    private final EnumMap<UserStatus, Set<UserStatus>> validTransitions;

    public UserStateFlow() {
        validTransitions = new EnumMap<>(UserStatus.class);

        // Initial state -> Active or Suspended
        validTransitions.put(UserStatus.PENDING, Set.of(
                UserStatus.ACTIVE,
                UserStatus.SUSPENDED
        ));

        // Active -> Suspended or Deactivated
        validTransitions.put(UserStatus.ACTIVE, Set.of(
                UserStatus.SUSPENDED,
                UserStatus.DEACTIVATED
        ));

        // Suspended -> Active or Deactivated
        validTransitions.put(UserStatus.SUSPENDED, Set.of(
                UserStatus.ACTIVE,
                UserStatus.DEACTIVATED
        ));

        // Terminal state
        validTransitions.put(UserStatus.DEACTIVATED, Set.of());
    }

    public boolean canTransition(UserStatus currentState, UserStatus newState) {
        return validTransitions.get(currentState).contains(newState);
    }

    public Set<UserStatus> getValidTransitions(UserStatus currentState) {
        return validTransitions.get(currentState);
    }

    public boolean isTerminalState(UserStatus state) {
        return validTransitions.get(state).isEmpty();
    }
}
