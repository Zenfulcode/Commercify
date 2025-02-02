package com.zenfulcode.commercify.user.domain.event;

import com.zenfulcode.commercify.shared.domain.event.DomainEvent;
import com.zenfulcode.commercify.shared.domain.valueobject.AggregateId;
import com.zenfulcode.commercify.user.domain.model.UserStatus;
import com.zenfulcode.commercify.user.domain.valueobject.UserId;
import lombok.Getter;

import java.time.Instant;

@Getter
public class UserStatusChangedEvent extends DomainEvent {
    @AggregateId
    private final UserId userId;
    private final UserStatus oldStatus;
    private final UserStatus newStatus;
    private final Instant changedAt;

    public UserStatusChangedEvent(
            Object source,
            UserId userId,
            UserStatus oldStatus,
            UserStatus newStatus
    ) {
        super(source);
        this.userId = userId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.changedAt = Instant.now();
    }

    public boolean isDeactivationTransition() {
        return newStatus == UserStatus.DEACTIVATED;
    }

    public boolean isSuspensionTransition() {
        return newStatus == UserStatus.SUSPENDED;
    }

    public boolean isActivationTransition() {
        return newStatus == UserStatus.ACTIVE;
    }

    @Override
    public String getEventType() {
        return "USER_STATUS_CHANGED";
    }
}