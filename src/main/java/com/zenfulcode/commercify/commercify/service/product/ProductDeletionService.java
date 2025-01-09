package com.zenfulcode.commercify.commercify.service.product;

import com.zenfulcode.commercify.commercify.OrderStatus;
import com.zenfulcode.commercify.commercify.dto.OrderDTO;
import com.zenfulcode.commercify.commercify.dto.ProductDeletionValidationResult;
import com.zenfulcode.commercify.commercify.dto.mapper.OrderMapper;
import com.zenfulcode.commercify.commercify.entity.ProductEntity;
import com.zenfulcode.commercify.commercify.exception.ProductDeletionException;
import com.zenfulcode.commercify.commercify.repository.OrderLineRepository;
import com.zenfulcode.commercify.commercify.repository.ProductRepository;
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

    public void validateAndDelete(ProductEntity product) {
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

    private ProductDeletionValidationResult validateDeletion(ProductEntity product) {
        List<OrderDTO> activeOrders = orderLineRepository
                .findActiveOrdersForProduct(
                        product.getId(),
                        List.of(OrderStatus.PENDING, OrderStatus.PAID, OrderStatus.SHIPPED)
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