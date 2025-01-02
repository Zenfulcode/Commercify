package com.zenfulcode.commercify.order.domain.valueobject;

import com.zenfulcode.commercify.order.domain.exception.OrderValidationException;
import com.zenfulcode.commercify.shared.domain.model.Money;

import java.util.ArrayList;
import java.util.List;

public record OrderDetails(
        Long customerId,
        String currency,
        CustomerDetails customerDetails,
        Address shippingAddress,
        Address billingAddress,
        List<OrderLineDetails> orderLines
) {
    public OrderDetails {
        validate(customerId, currency, customerDetails, shippingAddress, orderLines);
    }

    private void validate(
            Long customerId,
            String currency,
            CustomerDetails customerDetails,
            Address shippingAddress,
            List<OrderLineDetails> orderLines
    ) {
        List<String> violations = new ArrayList<>();

        if (customerId == null) {
            violations.add("Customer ID is required");
        }

        if (currency == null || currency.isBlank()) {
            violations.add("Currency is required");
        }

        if (customerDetails == null) {
            violations.add("Customer details are required");
        }

        if (shippingAddress == null) {
            violations.add("Shipping address is required");
        }

        if (orderLines == null || orderLines.isEmpty()) {
            violations.add("Order must contain at least one item");
        }

        if (!violations.isEmpty()) {
            throw new OrderValidationException(violations);
        }
    }

    public Money calculateSubtotal() {
        return orderLines.stream()
                .map(OrderLineDetails::calculateTotal)
                .reduce(Money.zero(currency), Money::add);
    }
}
