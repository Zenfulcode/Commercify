package com.zenfulcode.commercify.commercify.repository;

import com.zenfulcode.commercify.commercify.entity.PriceEntity;
import com.zenfulcode.commercify.commercify.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PriceRepository extends JpaRepository<PriceEntity, Long> {
    List<PriceEntity> findByProductAndActive(ProductEntity product, Boolean active);

    Optional<PriceEntity> findByProductAndIsDefault(ProductEntity product, Boolean isDefault);
}