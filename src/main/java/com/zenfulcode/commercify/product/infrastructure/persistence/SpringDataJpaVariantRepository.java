package com.zenfulcode.commercify.product.infrastructure.persistence;

import com.zenfulcode.commercify.product.domain.model.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

interface SpringDataJpaVariantRepository extends JpaRepository<ProductVariant, Long> {
    Optional<ProductVariant> findBySku(String sku);

    List<ProductVariant> findByProductId(String productId);
}
