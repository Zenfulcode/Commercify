package com.zenfulcode.commercify.order.domain.service;

import com.zenfulcode.commercify.order.domain.model.Order;
import com.zenfulcode.commercify.shared.domain.model.Money;

public interface OrderPricingStrategy {
    Money calculateSubtotal(Order order);

    Money calculateShippingCost(Order order);

    Money calculateTax(Order order);
}

