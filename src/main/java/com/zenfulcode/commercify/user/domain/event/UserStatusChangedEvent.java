package com.zenfulcode.commercify.user.domain.event;

import com.zenfulcode.commercify.shared.domain.event.DomainEvent;
import com.zenfulcode.commercify.shared.domain.valueobject.AggregateId;
import com.zenfulcode.commercify.user.domain.model.UserStatus;
import com.zenfulcode.commercify.user.domain.valueobject.UserId;
import lombok.Getter;

@Getter
public class UserStatusChangedEvent extends DomainEvent {
    @AggregateId
    private final UserId userId;
    private final UserStatus oldStatus;
    private final UserStatus newStatus;

    public UserStatusChangedEvent(
            UserId userId,
            UserStatus oldStatus,
            UserStatus newStatus
    ) {
        this.userId = userId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
    }
}
