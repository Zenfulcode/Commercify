package com.zenfulcode.commercify.order.application.command;

import com.zenfulcode.commercify.order.domain.valueobject.OrderId;

public record CancelOrderCommand(
        OrderId orderId
) {}