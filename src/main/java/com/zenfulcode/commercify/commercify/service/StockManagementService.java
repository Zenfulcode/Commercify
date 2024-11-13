package com.zenfulcode.commercify.commercify.service;

import com.zenfulcode.commercify.commercify.entity.OrderLineEntity;
import com.zenfulcode.commercify.commercify.entity.ProductEntity;
import com.zenfulcode.commercify.commercify.exception.ProductNotFoundException;
import com.zenfulcode.commercify.commercify.repository.ProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@AllArgsConstructor
public class StockManagementService {
    private final ProductRepository productRepository;

    @Transactional
    public void updateStockLevels(Set<OrderLineEntity> orderLines) {
        orderLines.forEach(line -> {
            ProductEntity product = productRepository.findById(line.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException(line.getProductId()));

            product.setStock(product.getStock() - line.getQuantity());
            productRepository.save(product);
        });
    }

    @Transactional
    public void restoreStockLevels(Set<OrderLineEntity> orderLines) {
        orderLines.forEach(line -> {
            ProductEntity product = productRepository.findById(line.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException(line.getProductId()));

            product.setStock(product.getStock() + line.getQuantity());
            productRepository.save(product);
        });
    }
}
