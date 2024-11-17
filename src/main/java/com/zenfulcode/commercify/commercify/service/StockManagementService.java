package com.zenfulcode.commercify.commercify.service;

import com.zenfulcode.commercify.commercify.entity.OrderLineEntity;
import com.zenfulcode.commercify.commercify.entity.ProductEntity;
import com.zenfulcode.commercify.commercify.entity.ProductVariantEntity;
import com.zenfulcode.commercify.commercify.exception.ProductNotFoundException;
import com.zenfulcode.commercify.commercify.repository.ProductRepository;
import com.zenfulcode.commercify.commercify.repository.ProductVariantRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@AllArgsConstructor
public class StockManagementService {
    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;

    @Transactional
    public void updateStockLevels(Set<OrderLineEntity> orderLines) {
        for (OrderLineEntity line : orderLines) {
            if (line.getProductVariant() != null) {
                // Update variant stock
                ProductVariantEntity variant = line.getProductVariant();
                variant.setStock(variant.getStock() - line.getQuantity());
                variantRepository.save(variant);
            } else {
                // Update product stock
                ProductEntity product = productRepository.findById(line.getProductId())
                        .orElseThrow(() -> new ProductNotFoundException(line.getProductId()));
                product.setStock(product.getStock() - line.getQuantity());
                productRepository.save(product);
            }
        }
    }

    @Transactional
    public void restoreStockLevels(Set<OrderLineEntity> orderLines) {
        for (OrderLineEntity line : orderLines) {
            if (line.getProductVariant() != null) {
                // Restore variant stock
                ProductVariantEntity variant = line.getProductVariant();
                variant.setStock(variant.getStock() + line.getQuantity());
                variantRepository.save(variant);
            } else {
                // Restore product stock
                ProductEntity product = productRepository.findById(line.getProductId())
                        .orElseThrow(() -> new ProductNotFoundException(line.getProductId()));
                product.setStock(product.getStock() + line.getQuantity());
                productRepository.save(product);
            }
        }
    }
}
