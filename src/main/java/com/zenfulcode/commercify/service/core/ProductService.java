package com.zenfulcode.commercify.service.core;

import com.zenfulcode.commercify.web.dto.request.product.ProductRequest;
import com.zenfulcode.commercify.domain.model.Product;
import com.zenfulcode.commercify.web.dto.common.ProductDTO;
import com.zenfulcode.commercify.web.dto.common.ProductUpdateResult;
import com.zenfulcode.commercify.web.dto.mapper.ProductMapper;
import com.zenfulcode.commercify.domain.model.ProductVariant;
import com.zenfulcode.commercify.exception.ProductNotFoundException;
import com.zenfulcode.commercify.component.factory.ProductFactory;
import com.zenfulcode.commercify.repository.ProductRepository;
import com.zenfulcode.commercify.service.ProductDeletionService;
import com.zenfulcode.commercify.service.validations.ProductValidationService;
import com.zenfulcode.commercify.service.ProductVariantService;
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
        Product product = productFactory.createFromRequest(request);

        if (request.variants() != null && !request.variants().isEmpty()) {
            Set<ProductVariant> variants = variantService.createVariantsFromRequest(request.variants(), product);
            product.setVariants(variants);
        }

        Product savedProduct = productRepository.save(product);
        return productMapper.apply(savedProduct);
    }

    @Transactional
    public ProductUpdateResult updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        updateProductDetails(product, request);
        Product savedProduct = productRepository.save(product);

        return ProductUpdateResult.withWarnings(
                productMapper.apply(savedProduct),
                Collections.emptyList()
        );
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        deletionService.validateAndDelete(product);
    }

    @Transactional
    public void reactivateProduct(Long id) {
        toggleProductStatus(id, true);
    }

    @Transactional
    public void deactivateProduct(Long id) {
        toggleProductStatus(id, false);
    }

    @Transactional(readOnly = true)
    public Page<ProductDTO> getAllProducts(PageRequest pageRequest) {
        return productRepository.findAll(pageRequest).map(productMapper);
    }

    @Transactional(readOnly = true)
    public ProductDTO getProductById(Long id) {
        return productRepository.findById(id)
                .map(productMapper)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public Page<ProductDTO> getActiveProducts(PageRequest pageRequest) {
        return productRepository.queryAllByActiveTrue(pageRequest).map(productMapper);
    }

    private void toggleProductStatus(Long id, boolean active) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        product.setActive(active);
        productRepository.save(product);
    }

    private void updateProductDetails(Product product, ProductRequest request) {
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