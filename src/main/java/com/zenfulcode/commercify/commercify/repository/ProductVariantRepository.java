package com.zenfulcode.commercify.commercify.repository;

import com.zenfulcode.commercify.commercify.entity.ProductVariantEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductVariantRepository extends JpaRepository<ProductVariantEntity, Integer> {
    Page<ProductVariantEntity> findByProductId(Integer productId, Pageable pageable);
}
