package com.zenfulcode.commercify.product.domain.repository;

import com.zenfulcode.commercify.product.domain.model.Product;
import com.zenfulcode.commercify.product.domain.valueobject.CategoryId;
import com.zenfulcode.commercify.product.domain.valueobject.ProductId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ProductRepository {
    Product save(Product product);

    Optional<Product> findById(ProductId id);

    void delete(Product product);

    Page<Product> findAll(Pageable pageable);

    Page<Product> findByActiveTrue(Pageable pageable);

    Page<Product> findByCategory(CategoryId categoryId, Pageable pageable);

    Page<Product> findByStockLessThan(int threshold, Pageable pageable);

    boolean existsBySku(String sku);
}
