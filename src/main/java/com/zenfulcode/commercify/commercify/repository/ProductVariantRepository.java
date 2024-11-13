package com.zenfulcode.commercify.commercify.repository;

import com.zenfulcode.commercify.commercify.entity.ProductVariantEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductVariantRepository extends JpaRepository<ProductVariantEntity, Long> {
    Page<ProductVariantEntity> findByProductId(Long productId, Pageable pageable);
}
