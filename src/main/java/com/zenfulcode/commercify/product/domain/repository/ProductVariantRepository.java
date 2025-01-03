package com.zenfulcode.commercify.product.domain.repository;

import com.zenfulcode.commercify.product.domain.model.ProductVariant;
import com.zenfulcode.commercify.product.domain.valueobject.ProductId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductVariantRepository {
    ProductVariant save(ProductVariant variant);
    Optional<ProductVariant> findById(Long id);
    Optional<ProductVariant> findBySku(String sku);
    void delete(ProductVariant variant);
    List<ProductVariant> findByProductId(ProductId productId);
}

