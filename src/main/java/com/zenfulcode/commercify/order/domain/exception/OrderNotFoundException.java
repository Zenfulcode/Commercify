package com.zenfulcode.commercify.order.domain.exception;

import com.zenfulcode.commercify.order.domain.valueobject.OrderId;
import com.zenfulcode.commercify.shared.domain.exception.DomainException;

public class OrderNotFoundException extends DomainException {
    public OrderNotFoundException(OrderId orderId) {
        super("Order not found with ID: " + orderId);
    }
}
