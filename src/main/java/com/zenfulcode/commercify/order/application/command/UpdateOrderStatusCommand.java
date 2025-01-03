package com.zenfulcode.commercify.order.application.command;

import com.zenfulcode.commercify.order.domain.model.OrderStatus;
import com.zenfulcode.commercify.order.domain.valueobject.OrderId;

public record UpdateOrderStatusCommand(
        OrderId orderId,
        OrderStatus newStatus
) {}
