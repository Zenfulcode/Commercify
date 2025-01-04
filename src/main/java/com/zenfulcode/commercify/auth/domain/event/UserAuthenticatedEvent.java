package com.zenfulcode.commercify.auth.domain.event;

import com.zenfulcode.commercify.shared.domain.event.DomainEvent;
import com.zenfulcode.commercify.shared.domain.valueobject.AggregateId;
import com.zenfulcode.commercify.user.domain.valueobject.UserId;
import lombok.Getter;

@Getter
public class UserAuthenticatedEvent extends DomainEvent {
    @AggregateId
    private final UserId userId;
    private final String username;

    public UserAuthenticatedEvent(UserId userId, String username) {
        this.userId = userId;
        this.username = username;
    }
}
