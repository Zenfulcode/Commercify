package com.zenfulcode.commercify.order.domain.service;

import com.zenfulcode.commercify.order.domain.model.Order;
import com.zenfulcode.commercify.order.domain.model.OrderLine;
import com.zenfulcode.commercify.shared.domain.model.Money;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class DefaultOrderPricingStrategy implements OrderPricingStrategy {
    //    TODO: Currency conversion
    private static final BigDecimal TAX_RATE = new BigDecimal("0.20"); // 20% tax
    private static final Money FREE_SHIPPING_THRESHOLD = Money.of(new BigDecimal("100"), "USD");
    private static final Money STANDARD_SHIPPING = Money.of(new BigDecimal("10"), "USD");

    @Override
    public Money calculateSubtotal(Order order) {
        return order.getOrderLines().stream()
                .map(OrderLine::getTotal)
                .reduce(Money.zero(order.getCurrency()), Money::add);
    }

    @Override
    public Money calculateShippingCost(Order order) {
        Money subtotal = calculateSubtotal(order);
        if (subtotal.isGreaterThanOrEqual(FREE_SHIPPING_THRESHOLD)) {
            return Money.zero(order.getCurrency());
        }
        return STANDARD_SHIPPING;
    }

    @Override
    public Money calculateTax(Order order) {
        Money subtotal = calculateSubtotal(order);
        return subtotal.multiply(TAX_RATE);
    }
}
