package com.zenfulcode.commercify.product.domain.repository;

import com.zenfulcode.commercify.product.domain.model.Product;
import com.zenfulcode.commercify.product.domain.model.ProductVariant;
import com.zenfulcode.commercify.product.domain.valueobject.CategoryId;
import com.zenfulcode.commercify.product.domain.valueobject.ProductId;
import com.zenfulcode.commercify.product.domain.valueobject.VariantId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    Product save(Product product);

    Optional<Product> findById(ProductId id);

    void delete(Product product);

    Page<Product> findAll(Pageable pageable);

    Page<Product> findByActiveTrue(Pageable pageable);

    Page<Product> findByCategory(CategoryId categoryId, Pageable pageable);

    Page<Product> findByStockLessThan(int threshold, Pageable pageable);

    List<Product> findAllById(Collection<ProductId> ids);

    List<ProductVariant> findVariantsByIds(Collection<VariantId> variantIds);

    int findNewProducts(Instant startDate, Instant endDate);
}
