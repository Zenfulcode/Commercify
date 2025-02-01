package com.zenfulcode.commercify.user.domain.event;

import com.zenfulcode.commercify.shared.domain.event.DomainEvent;
import com.zenfulcode.commercify.shared.domain.valueobject.AggregateId;
import com.zenfulcode.commercify.user.domain.model.UserStatus;
import com.zenfulcode.commercify.user.domain.valueobject.UserId;
import lombok.Getter;

@Getter
public class UserCreatedEvent extends DomainEvent {
    @AggregateId
    private final UserId userId;
    private final String email;
    private final UserStatus status;

    public UserCreatedEvent(Object source, UserId userId, String email, UserStatus status) {
        super(source);
        this.userId = userId;
        this.email = email;
        this.status = status;
    }
}
