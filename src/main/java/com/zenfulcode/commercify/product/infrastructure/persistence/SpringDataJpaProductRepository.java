package com.zenfulcode.commercify.product.infrastructure.persistence;

import com.zenfulcode.commercify.product.domain.model.Product;
import com.zenfulcode.commercify.product.domain.valueobject.CategoryId;
import com.zenfulcode.commercify.product.domain.valueobject.ProductId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface SpringDataJpaProductRepository extends JpaRepository<Product, ProductId> {
    Page<Product> findByActiveTrue(Pageable pageable);

    Page<Product> findByCategoryId(CategoryId categoryId, Pageable pageable);

    Page<Product> findByStockLessThan(int threshold, Pageable pageable);
}