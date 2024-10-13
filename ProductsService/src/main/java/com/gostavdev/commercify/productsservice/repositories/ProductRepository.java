package com.gostavdev.commercify.productsservice.repositories;

import com.gostavdev.commercify.productsservice.entities.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Long> {
    Page<ProductEntity> queryAllByActiveTrue(Pageable pageable);
}
