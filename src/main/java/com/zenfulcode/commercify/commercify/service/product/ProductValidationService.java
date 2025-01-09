package com.zenfulcode.commercify.commercify.service.product;

import com.zenfulcode.commercify.commercify.OrderStatus;
import com.zenfulcode.commercify.commercify.api.requests.products.ProductRequest;
import com.zenfulcode.commercify.commercify.api.requests.products.ProductVariantRequest;
import com.zenfulcode.commercify.commercify.dto.OrderDTO;
import com.zenfulcode.commercify.commercify.dto.mapper.OrderMapper;
import com.zenfulcode.commercify.commercify.entity.OrderEntity;
import com.zenfulcode.commercify.commercify.entity.ProductVariantEntity;
import com.zenfulcode.commercify.commercify.exception.ProductDeletionException;
import com.zenfulcode.commercify.commercify.exception.ProductValidationException;
import com.zenfulcode.commercify.commercify.repository.OrderLineRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@AllArgsConstructor
public class ProductValidationService {
    private final OrderLineRepository orderLineRepository;
    private final OrderMapper orderMapper;

    public void validateProductRequest(ProductRequest request) {
        List<String> errors = new ArrayList<>();

        if (request.name() == null || request.name().isBlank()) {
            errors.add("Product name is required");
        }
        if (request.price() == null || request.price().amount() == null || request.price().amount() < 0) {
            errors.add("Valid unitPrice is required");
        }
        if (request.stock() != null && request.stock() < 0) {
            errors.add("Stock cannot be negative");
        }

        if (!errors.isEmpty()) {
            throw new ProductValidationException(errors);
        }
    }

    public void validateVariantRequest(ProductVariantRequest request) {
        List<String> errors = new ArrayList<>();

        if (request.sku() == null || request.sku().isBlank()) {
            errors.add("SKU is required");
        }
        if (request.options() == null || request.options().isEmpty()) {
            errors.add("At least one variant option is required");
        }

        if (!errors.isEmpty()) {
            throw new ProductValidationException(errors);
        }
    }

    public void validateVariantDeletion(ProductVariantEntity variant) {
        Set<OrderEntity> activeOrders = orderLineRepository.findActiveOrdersForVariant(
                variant.getId(),
                List.of(OrderStatus.PENDING, OrderStatus.PAID, OrderStatus.SHIPPED)
        );

        if (!activeOrders.isEmpty()) {
            List<OrderDTO> activeOrderDTOs = activeOrders.stream()
                    .map(orderMapper)
                    .toList();

            throw new ProductDeletionException(
                    "Cannot delete variant with active orders",
                    List.of("Variant has active orders"),
                    activeOrderDTOs
            );
        }
    }
}