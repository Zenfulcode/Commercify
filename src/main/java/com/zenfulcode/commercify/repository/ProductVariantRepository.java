package com.zenfulcode.commercify.repository;

import com.zenfulcode.commercify.domain.model.ProductVariant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    Page<ProductVariant> findByProductId(Long productId, Pageable pageable);
}
