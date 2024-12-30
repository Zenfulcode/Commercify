package com.zenfulcode.commercify.service;

import com.zenfulcode.commercify.web.dto.request.product.CreateVariantOptionRequest;
import com.zenfulcode.commercify.web.dto.request.product.ProductVariantRequest;
import com.zenfulcode.commercify.domain.model.Product;
import com.zenfulcode.commercify.web.dto.common.ProductVariantEntityDto;
import com.zenfulcode.commercify.web.dto.mapper.ProductVariantMapper;
import com.zenfulcode.commercify.domain.model.ProductVariant;
import com.zenfulcode.commercify.domain.model.VariantOption;
import com.zenfulcode.commercify.exception.ProductNotFoundException;
import com.zenfulcode.commercify.repository.ProductRepository;
import com.zenfulcode.commercify.repository.ProductVariantRepository;
import com.zenfulcode.commercify.service.validations.ProductValidationService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ProductVariantService {
    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final ProductVariantMapper variantMapper;
    private final ProductValidationService validationService;

    @Transactional
    public ProductVariantEntityDto addVariant(Long productId, ProductVariantRequest request) {
        validationService.validateVariantRequest(request);
        Product product = getProduct(productId);

        ProductVariant variant = createVariantFromRequest(request, product);
        product.addVariant(variant);

        ProductVariant savedVariant = variantRepository.save(variant);
        return variantMapper.apply(savedVariant);
    }

    @Transactional
    public ProductVariantEntityDto updateVariant(Long productId, Long variantId, ProductVariantRequest request) {
        validationService.validateVariantRequest(request);

        ProductVariant variant = getVariant(productId, variantId);
        updateVariantDetails(variant, request);

        ProductVariant savedVariant = variantRepository.save(variant);
        return variantMapper.apply(savedVariant);
    }

    @Transactional
    public void deleteVariant(Long productId, Long variantId) {
        ProductVariant variant = getVariant(productId, variantId);
        validationService.validateVariantDeletion(variant);
        variantRepository.delete(variant);
    }

    @Transactional(readOnly = true)
    public Page<ProductVariantEntityDto> getProductVariants(Long productId, PageRequest pageRequest) {
        // Verify product exists
        getProduct(productId);
        return variantRepository.findByProductId(productId, pageRequest)
                .map(variantMapper);
    }

    public ProductVariantEntityDto getVariantDto(Long productId, Long variantId) {
        ProductVariant variant = getVariant(productId, variantId);
        Product product = variant.getProduct();

        ProductVariantEntityDto dto = variantMapper.apply(variant);

        // Apply inheritance only when retrieving
        dto.setStock(variant.getStock() != null ? variant.getStock() : product.getStock());
        dto.setImageUrl(variant.getImageUrl() != null ? variant.getImageUrl() : product.getImageUrl());
        dto.setUnitPrice(variant.getUnitPrice() != null ? variant.getUnitPrice() : product.getUnitPrice());

        return dto;
    }

    @Transactional(readOnly = true)
    public Set<ProductVariant> createVariantsFromRequest(List<ProductVariantRequest> requests, Product product) {
        return requests.stream()
                .map(request -> createVariantFromRequest(request, product))
                .collect(Collectors.toSet());
    }

    private Product getProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
    }

    private ProductVariant getVariant(Long productId, Long variantId) {
        Product product = getProduct(productId);
        return product.getVariants().stream()
                .filter(variant -> variant.getId().equals(variantId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                        String.format("Variant %d not found for product %d", variantId, product.getId())
                ));
    }

    private ProductVariant createVariantFromRequest(ProductVariantRequest request, Product product) {
        ProductVariant variant = new ProductVariant();
        updateVariantDetails(variant, request);
        variant.setProduct(product);
        return variant;
    }

    private void updateVariantDetails(ProductVariant variant, ProductVariantRequest request) {
        variant.setSku(request.sku());
        variant.setStock(request.stock());
        variant.setImageUrl(request.imageUrl());
        variant.setUnitPrice(request.unitPrice());
        updateVariantOptions(variant, request.options());
    }

    private void updateVariantOptions(ProductVariant variant, List<CreateVariantOptionRequest> options) {
        variant.getOptions().clear();
        options.forEach(optionRequest -> {
            VariantOption option = VariantOption.builder()
                    .name(optionRequest.name())
                    .value(optionRequest.value())
                    .productVariant(variant)
                    .build();
            variant.addOption(option);
        });
    }
}