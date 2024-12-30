package com.zenfulcode.commercify.service;

import com.zenfulcode.commercify.domain.enums.OrderStatus;
import com.zenfulcode.commercify.domain.model.Product;
import com.zenfulcode.commercify.web.dto.common.OrderDTO;
import com.zenfulcode.commercify.web.dto.common.ProductDeletionValidationResult;
import com.zenfulcode.commercify.web.dto.mapper.OrderMapper;
import com.zenfulcode.commercify.exception.ProductDeletionException;
import com.zenfulcode.commercify.repository.OrderLineRepository;
import com.zenfulcode.commercify.repository.ProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class ProductDeletionService {
    private final OrderLineRepository orderLineRepository;
    private final OrderMapper orderMapper;
    private final ProductRepository productRepository;

    public void validateAndDelete(Product product) {
        ProductDeletionValidationResult validationResult = validateDeletion(product);

        if (!validationResult.canDelete()) {
            throw new ProductDeletionException(
                    "Cannot delete product",
                    validationResult.getIssues(),
                    validationResult.getActiveOrders()
            );
        }

        productRepository.delete(product);
    }

    private ProductDeletionValidationResult validateDeletion(Product product) {
        List<OrderDTO> activeOrders = orderLineRepository
                .findActiveOrdersForProduct(
                        product.getId(),
                        List.of(OrderStatus.PENDING, OrderStatus.CONFIRMED, OrderStatus.SHIPPED)
                )
                .stream()
                .map(orderMapper)
                .toList();

        List<String> issues = new ArrayList<>();
        if (!activeOrders.isEmpty()) {
            issues.add(String.format("Product has %d active orders", activeOrders.size()));
        }

        return new ProductDeletionValidationResult(issues.isEmpty(), issues, activeOrders);
    }
}