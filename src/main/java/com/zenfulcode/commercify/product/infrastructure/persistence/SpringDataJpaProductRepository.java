package com.zenfulcode.commercify.product.infrastructure.persistence;

import com.zenfulcode.commercify.product.domain.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface SpringDataJpaProductRepository extends JpaRepository<Product, String> {
    Page<Product> findByActiveTrue(Pageable pageable);

    Page<Product> findByCategoryId(String categoryId, Pageable pageable);

    Page<Product> findByStockLessThan(int threshold, Pageable pageable);

    boolean existsBySku(String sku);
}
