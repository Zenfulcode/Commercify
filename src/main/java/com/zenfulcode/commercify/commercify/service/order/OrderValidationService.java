package com.zenfulcode.commercify.commercify.service.order;

import com.zenfulcode.commercify.commercify.OrderStatus;
import com.zenfulcode.commercify.commercify.PaymentStatus;
import com.zenfulcode.commercify.commercify.api.requests.orders.CreateOrderRequest;
import com.zenfulcode.commercify.commercify.entity.OrderEntity;
import com.zenfulcode.commercify.commercify.entity.ProductEntity;
import com.zenfulcode.commercify.commercify.exception.InsufficientStockException;
import com.zenfulcode.commercify.commercify.exception.OrderValidationException;
import com.zenfulcode.commercify.commercify.flow.OrderStateFlow;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class OrderValidationService {
    private OrderStateFlow orderStateFlow;

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

        if (request.shippingAddress() == null) {
            errors.add("Shipping address is required");
        }

        if (request.billingAddress() != null) {
            if (request.billingAddress().getStreet() == null || request.billingAddress().getStreet().isBlank()) {
                errors.add("Billing address street is required");
            }
            if (request.billingAddress().getCity() == null || request.billingAddress().getCity().isBlank()) {
                errors.add("Billing address city is required");
            }

            if (request.billingAddress().getZipCode() == null || request.billingAddress().getZipCode().isBlank()) {
                errors.add("Billing address zip code is required");
            }
            if (request.billingAddress().getCountry() == null || request.billingAddress().getCountry().isBlank()) {
                errors.add("Billing address country is required");
            }
        }

        if (request.customerDetails() == null) {
            errors.add("Customer details are required");
        }

        if (request.customerDetails() != null) {
            if (request.customerDetails().getFirstName() == null || request.customerDetails().getFirstName().isBlank()) {
                errors.add("Customer first name is required");
            }
            if (request.customerDetails().getLastName() == null || request.customerDetails().getLastName().isBlank()) {
                errors.add("Customer last name is required");
            }
            if (request.customerDetails().getEmail() == null || request.customerDetails().getEmail().isBlank()) {
                errors.add("Customer email is required");
            }
        }

        if (!errors.isEmpty()) {
            throw new OrderValidationException("Order validation failed: " + String.join(", ", errors));
        }
    }

    public void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        if (!orderStateFlow.canTransition(currentStatus, newStatus)) {
            throw new IllegalStateException(
                    String.format("Invalid status transition from %s to %s", currentStatus, newStatus)
            );
        }
    }

    public void validateOrderCancellation(OrderEntity order) {
        if (orderStateFlow.canTransition(order.getStatus(), OrderStatus.CANCELLED)) {
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

    public OrderStatus mapOrderStatus(PaymentStatus status) {
        return switch (status) {
            case PENDING -> OrderStatus.PENDING;
            case PAID -> OrderStatus.PAID;
            case FAILED, NOT_FOUND -> OrderStatus.FAILED;
            case CANCELLED, TERMINATED, EXPIRED -> OrderStatus.CANCELLED;
            case REFUNDED -> OrderStatus.REFUNDED;
        };
    }

}