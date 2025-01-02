package com.zenfulcode.commercify.product.infrastructure.persistence;

import com.zenfulcode.commercify.product.domain.model.ProductVariant;
import com.zenfulcode.commercify.product.domain.repository.ProductVariantRepository;
import com.zenfulcode.commercify.product.domain.valueobject.ProductId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
class JpaProductVariantRepository implements ProductVariantRepository {
    private final SpringDataJpaVariantRepository repository;

    JpaProductVariantRepository(SpringDataJpaVariantRepository repository) {
        this.repository = repository;
    }

    @Override
    public ProductVariant save(ProductVariant variant) {
        return null;
    }

    @Override
    public Optional<ProductVariant> findById(Long id) {
        return Optional.empty();
    }

    @Override
    public Optional<ProductVariant> findBySku(String sku) {
        return Optional.empty();
    }

    @Override
    public void delete(ProductVariant variant) {

    }

    @Override
    public List<ProductVariant> findByProductId(ProductId productId) {
        return List.of();
    }
}
