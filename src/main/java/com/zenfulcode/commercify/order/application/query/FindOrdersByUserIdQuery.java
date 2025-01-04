package com.zenfulcode.commercify.order.application.query;

import com.zenfulcode.commercify.user.domain.valueobject.UserId;
import org.springframework.data.domain.PageRequest;

public record FindOrdersByUserIdQuery(
        UserId userId,
        PageRequest pageRequest
) {
}
