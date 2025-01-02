package com.zenfulcode.commercify.product.infrastructure.persistence;


import com.zenfulcode.commercify.product.domain.model.Product;
import com.zenfulcode.commercify.product.domain.repository.ProductRepository;
import com.zenfulcode.commercify.product.domain.valueobject.CategoryId;
import com.zenfulcode.commercify.product.domain.valueobject.ProductId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JpaProductRepository implements ProductRepository {
    private final SpringDataJpaProductRepository repository;

    JpaProductRepository(SpringDataJpaProductRepository repository) {
        this.repository = repository;
    }

    @Override
    public Product save(Product product) {
        return repository.save(product);
    }

    @Override
    public Optional<Product> findById(ProductId id) {
        return repository.findById(id.getValue());
    }

    @Override
    public void delete(Product product) {

    }

    @Override
    public Page<Product> findAll(Pageable pageable) {
        return null;
    }

    @Override
    public Page<Product> findByActiveTrue(Pageable pageable) {
        return null;
    }

    @Override
    public Page<Product> findByCategory(CategoryId categoryId, Pageable pageable) {
        return repository.findByCategoryId(categoryId.getValue(), pageable);
    }

    @Override
    public Page<Product> findByStockLessThan(int threshold, Pageable pageable) {
        return null;
    }

    @Override
    public boolean existsBySku(String sku) {
        return false;
    }
}
