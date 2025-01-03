package com.zenfulcode.commercify.product.infrastructure.persistence;


import com.zenfulcode.commercify.product.domain.model.Product;
import com.zenfulcode.commercify.product.domain.model.ProductVariant;
import com.zenfulcode.commercify.product.domain.repository.ProductRepository;
import com.zenfulcode.commercify.product.domain.valueobject.CategoryId;
import com.zenfulcode.commercify.product.domain.valueobject.ProductId;
import com.zenfulcode.commercify.product.domain.valueobject.VariantId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public class JpaProductRepository implements ProductRepository {
    private final SpringDataJpaProductRepository repository;
    private final SpringDataJpaVariantRepository variantRepository;

    JpaProductRepository(SpringDataJpaProductRepository repository, SpringDataJpaVariantRepository variantRepository) {
        this.repository = repository;
        this.variantRepository = variantRepository;
    }

    @Override
    public Product save(Product product) {
        return repository.save(product);
    }

    @Override
    public Optional<Product> findById(ProductId id) {
        return repository.findById(id);
    }

    @Override
    public void delete(Product product) {
        repository.delete(product);
    }

    @Override
    public Page<Product> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Override
    public Page<Product> findByActiveTrue(Pageable pageable) {
        return repository.findByActiveTrue(pageable);
    }

    @Override
    public Page<Product> findByCategory(CategoryId categoryId, Pageable pageable) {
        return repository.findByCategoryId(categoryId, pageable);
    }

    @Override
    public Page<Product> findByStockLessThan(int threshold, Pageable pageable) {
        return repository.findByStockLessThan(threshold, pageable);
    }

    @Override
    public List<Product> findAllById(Collection<ProductId> ids) {
        return repository.findAllById(ids);
    }

    @Override
    public List<ProductVariant> findVariantsByIds(Collection<VariantId> variantIds) {
        return variantRepository.findAllById(variantIds);
    }
}
