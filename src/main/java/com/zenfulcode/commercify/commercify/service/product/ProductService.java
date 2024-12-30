package com.zenfulcode.commercify.commercify.service.product;

import com.zenfulcode.commercify.commercify.api.requests.products.ProductRequest;
import com.zenfulcode.commercify.commercify.dto.ProductDTO;
import com.zenfulcode.commercify.commercify.dto.ProductUpdateResult;
import com.zenfulcode.commercify.commercify.dto.mapper.ProductMapper;
import com.zenfulcode.commercify.commercify.entity.ProductEntity;
import com.zenfulcode.commercify.commercify.entity.ProductVariantEntity;
import com.zenfulcode.commercify.commercify.exception.ProductNotFoundException;
import com.zenfulcode.commercify.commercify.factory.ProductFactory;
import com.zenfulcode.commercify.commercify.repository.ProductRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Set;

@Service
@AllArgsConstructor
@Slf4j
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final ProductFactory productFactory;
    private final ProductValidationService validationService;
    private final ProductVariantService variantService;
    private final ProductDeletionService deletionService;

    @Transactional
    public ProductDTO saveProduct(ProductRequest request) {
        validationService.validateProductRequest(request);
        ProductEntity product = productFactory.createFromRequest(request);

        if (request.variants() != null && !request.variants().isEmpty()) {
            Set<ProductVariantEntity> variants = variantService.createVariantsFromRequest(request.variants(), product);
            product.setVariants(variants);
        }

        ProductEntity savedProduct = productRepository.save(product);
        return productMapper.apply(savedProduct);
    }

    @Transactional
    public ProductUpdateResult updateProduct(Integer id, ProductRequest request) {
        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        updateProductDetails(product, request);
        ProductEntity savedProduct = productRepository.save(product);

        return ProductUpdateResult.withWarnings(
                productMapper.apply(savedProduct),
                Collections.emptyList()
        );
    }

    @Transactional
    public void deleteProduct(Integer id) {
        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        deletionService.validateAndDelete(product);
    }

    @Transactional
    public void reactivateProduct(Integer id) {
        toggleProductStatus(id, true);
    }

    @Transactional
    public void deactivateProduct(Integer id) {
        toggleProductStatus(id, false);
    }

    @Transactional(readOnly = true)
    public Page<ProductDTO> getAllProducts(PageRequest pageRequest) {
        return productRepository.findAll(pageRequest).map(productMapper);
    }

    @Transactional(readOnly = true)
    public ProductDTO getProductById(Integer id) {
        return productRepository.findById(id)
                .map(productMapper)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public Page<ProductDTO> getActiveProducts(PageRequest pageRequest) {
        return productRepository.queryAllByActiveTrue(pageRequest).map(productMapper);
    }

    private void toggleProductStatus(Integer id, boolean active) {
        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        product.setActive(active);
        productRepository.save(product);
    }

    private void updateProductDetails(ProductEntity product, ProductRequest request) {
        if (request.name() != null) product.setName(request.name());
        if (request.description() != null) product.setDescription(request.description());
        if (request.stock() != null) product.setStock(request.stock());
        if (request.active() != null) product.setActive(request.active());
        if (request.imageUrl() != null) product.setImageUrl(request.imageUrl());
        if (request.price() != null) {
            product.setUnitPrice(request.price().amount());
            product.setCurrency(request.price().currency());
        }
    }
}