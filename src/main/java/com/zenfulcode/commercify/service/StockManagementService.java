package com.zenfulcode.commercify.service;

import com.zenfulcode.commercify.domain.model.OrderLine;
import com.zenfulcode.commercify.domain.model.Product;
import com.zenfulcode.commercify.domain.model.ProductVariant;
import com.zenfulcode.commercify.exception.ProductNotFoundException;
import com.zenfulcode.commercify.repository.ProductRepository;
import com.zenfulcode.commercify.repository.ProductVariantRepository;
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
    public void updateStockLevels(Set<OrderLine> orderLines) {
        for (OrderLine line : orderLines) {
            if (line.getProductVariant() != null) {
                // Update variant stock
                ProductVariant variant = line.getProductVariant();
                variant.setStock(variant.getStock() - line.getQuantity());
                variantRepository.save(variant);
            } else {
                // Update product stock
                Product product = productRepository.findById(line.getProductId())
                        .orElseThrow(() -> new ProductNotFoundException(line.getProductId()));
                product.setStock(product.getStock() - line.getQuantity());
                productRepository.save(product);
            }
        }
    }

    @Transactional
    public void restoreStockLevels(Set<OrderLine> orderLines) {
        for (OrderLine line : orderLines) {
            if (line.getProductVariant() != null) {
                // Restore variant stock
                ProductVariant variant = line.getProductVariant();
                variant.setStock(variant.getStock() + line.getQuantity());
                variantRepository.save(variant);
            } else {
                // Restore product stock
                Product product = productRepository.findById(line.getProductId())
                        .orElseThrow(() -> new ProductNotFoundException(line.getProductId()));
                product.setStock(product.getStock() + line.getQuantity());
                productRepository.save(product);
            }
        }
    }
}
