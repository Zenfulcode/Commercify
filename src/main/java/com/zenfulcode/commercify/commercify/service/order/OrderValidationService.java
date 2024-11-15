package com.zenfulcode.commercify.commercify.service.order;

import com.zenfulcode.commercify.commercify.OrderStatus;
import com.zenfulcode.commercify.commercify.api.requests.orders.CreateOrderRequest;
import com.zenfulcode.commercify.commercify.entity.OrderEntity;
import com.zenfulcode.commercify.commercify.entity.ProductEntity;
import com.zenfulcode.commercify.commercify.exception.InsufficientStockException;
import com.zenfulcode.commercify.commercify.exception.OrderValidationException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@AllArgsConstructor
public class OrderValidationService {
    private static final Map<OrderStatus, Set<OrderStatus>> VALID_TRANSITIONS = new HashMap<>();

    static {
        VALID_TRANSITIONS.put(OrderStatus.PENDING, Set.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED));
        VALID_TRANSITIONS.put(OrderStatus.CONFIRMED, Set.of(OrderStatus.SHIPPED, OrderStatus.CANCELLED));
        VALID_TRANSITIONS.put(OrderStatus.SHIPPED, Set.of(OrderStatus.COMPLETED, OrderStatus.RETURNED));
        VALID_TRANSITIONS.put(OrderStatus.COMPLETED, Set.of(OrderStatus.RETURNED));
        VALID_TRANSITIONS.put(OrderStatus.CANCELLED, Collections.emptySet());
        VALID_TRANSITIONS.put(OrderStatus.RETURNED, Collections.emptySet());
    }

    public void validateCreateOrderRequest(CreateOrderRequest request) {
        List<String> errors = new ArrayList<>();
        
        if (request.orderLines() == null || request.orderLines().isEmpty()) {
            errors.add("Order must contain at least one item");
        } else {
            request.orderLines().forEach(line -> {
                if (line.quantity() <= 0) {
                    errors.add("Quantity must be greater than 0 for product: " + line.productId());
                }
            });
        }
        if (request.currency() == null || request.currency().isBlank()) {
            errors.add("Currency is required");
        }

        if (!errors.isEmpty()) {
            throw new OrderValidationException("Order validation failed: " + String.join(", ", errors));
        }
    }

    public void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        Set<OrderStatus> validNextStatuses = VALID_TRANSITIONS.get(currentStatus);
        if (!validNextStatuses.contains(newStatus)) {
            throw new IllegalStateException(
                    String.format("Cannot transition from %s to %s", currentStatus, newStatus)
            );
        }
    }

    public void validateOrderCancellation(OrderEntity order) {
        if (order.getStatus() == OrderStatus.SHIPPED ||
                order.getStatus() == OrderStatus.COMPLETED ||
                order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException(
                    String.format("Cannot cancel order in status: %s", order.getStatus())
            );
        }
    }

    public void validateStockAvailability(ProductEntity product, int requestedQuantity) {
        if (product.getStock() < requestedQuantity) {
            throw new InsufficientStockException(
                    String.format("Insufficient stock for product %d. Available: %d, Requested: %d",
                            product.getId(), product.getStock(), requestedQuantity)
            );
        }
    }
}