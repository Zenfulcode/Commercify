package com.zenfulcode.commercify.order.domain.service;

import com.zenfulcode.commercify.order.domain.exception.InvalidOrderStateTransitionException;
import com.zenfulcode.commercify.order.domain.exception.OrderValidationException;
import com.zenfulcode.commercify.order.domain.model.Order;
import com.zenfulcode.commercify.order.domain.model.OrderLine;
import com.zenfulcode.commercify.order.domain.model.OrderStatus;
import com.zenfulcode.commercify.order.domain.valueobject.OrderLineDetails;
import com.zenfulcode.commercify.product.domain.exception.InsufficientStockException;
import com.zenfulcode.commercify.product.domain.exception.VariantNotFoundException;
import com.zenfulcode.commercify.product.domain.model.Product;
import com.zenfulcode.commercify.product.domain.model.ProductVariant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OrderValidationService {
    private final OrderStateFlow stateFlow;

    public void validateCreateOrder(Order order) {
        List<String> violations = new ArrayList<>();

        if (order.getOrderLines().isEmpty()) {
            violations.add("Order must contain at least one item");
        }

        if (order.getCurrency() == null || order.getCurrency().isBlank()) {
            violations.add("Currency is required");
        }

        if (order.getOrderShippingInfo() == null) {
            violations.add("Shipping information is required");
        }

        validateOrderLines(order.getOrderLines(), violations);

        if (!violations.isEmpty()) {
            throw new OrderValidationException(violations);
        }
    }

    private void validateOrderLines(Set<OrderLine> orderLines, List<String> violations) {
        for (OrderLine line : orderLines) {
            if (line.getQuantity() <= 0) {
                violations.add("Quantity must be greater than 0 for product: " + line.getProductId());
            }
            if (!line.getUnitPrice().isPositive()) {
                violations.add("Unit price must be greater than 0 for product: " + line.getProductId());
            }
        }
    }

    public void validateStatusTransition(Order order, OrderStatus newStatus) {
        if (!stateFlow.canTransition(order.getStatus(), newStatus)) {
            throw new InvalidOrderStateTransitionException(
                    order.getId(),
                    order.getStatus(),
                    newStatus,
                    "Invalid status transition"
            );
        }
    }

    public void validateOrderCancellation(Order order) {
        if (order.isInTerminalState(stateFlow)) {
            throw new OrderValidationException(
                    "Cannot cancel order in terminal status: " + order.getStatus()
            );
        }
    }

    public void validateOrderCompletion(Order order) {
        if (order.getStatus() != OrderStatus.SHIPPED) {
            throw new OrderValidationException(
                    "Cannot complete order that hasn't been shipped"
            );
        }
    }

    public void validateStock(Product product, int requestedQuantity) {
        if (!product.hasEnoughStock(requestedQuantity)) {
            throw new InsufficientStockException(
                    product.getId(),
                    requestedQuantity,
                    product.getStock()
            );
        }
    }

    public void validateVariant(ProductVariant variant, Product product, OrderLineDetails lineDetails) {
        List<String> violations = new ArrayList<>();

        if (variant == null) {
            throw new VariantNotFoundException(lineDetails.variantId());
        }

        if (!variant.belongsTo(product)) {
            violations.add(String.format("Variant %s does not belong to product %s",
                    variant.getId(), product.getId()));
        }

        if (!variant.hasEnoughStock(lineDetails.quantity())) {
            throw new InsufficientStockException(
                    variant.getId(),
                    lineDetails.quantity(),
                    variant.getStock()
            );
        }

        if (!violations.isEmpty()) {
            throw new OrderValidationException(violations);
        }
    }
}
