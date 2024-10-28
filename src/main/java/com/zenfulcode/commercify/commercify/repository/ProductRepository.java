package com.zenfulcode.commercify.commercify.repository;

import com.zenfulcode.commercify.commercify.entity.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Long> {
    Page<ProductEntity> queryAllByActiveTrue(Pageable pageable);
}
