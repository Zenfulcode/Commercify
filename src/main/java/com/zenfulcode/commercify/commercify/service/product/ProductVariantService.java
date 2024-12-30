package com.zenfulcode.commercify.commercify.service.product;

import com.zenfulcode.commercify.commercify.api.requests.products.CreateVariantOptionRequest;
import com.zenfulcode.commercify.commercify.api.requests.products.ProductVariantRequest;
import com.zenfulcode.commercify.commercify.dto.ProductVariantEntityDto;
import com.zenfulcode.commercify.commercify.dto.mapper.ProductVariantMapper;
import com.zenfulcode.commercify.commercify.entity.ProductEntity;
import com.zenfulcode.commercify.commercify.entity.ProductVariantEntity;
import com.zenfulcode.commercify.commercify.entity.VariantOptionEntity;
import com.zenfulcode.commercify.commercify.exception.ProductNotFoundException;
import com.zenfulcode.commercify.commercify.repository.ProductRepository;
import com.zenfulcode.commercify.commercify.repository.ProductVariantRepository;
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
    public ProductVariantEntityDto addVariant(Integer productId, ProductVariantRequest request) {
        validationService.validateVariantRequest(request);
        ProductEntity product = getProduct(productId);

        ProductVariantEntity variant = createVariantFromRequest(request, product);
        product.addVariant(variant);

        ProductVariantEntity savedVariant = variantRepository.save(variant);
        return variantMapper.apply(savedVariant);
    }

    @Transactional
    public ProductVariantEntityDto updateVariant(Integer productId, Integer variantId, ProductVariantRequest request) {
        validationService.validateVariantRequest(request);

        ProductVariantEntity variant = getVariant(productId, variantId);
        updateVariantDetails(variant, request);

        ProductVariantEntity savedVariant = variantRepository.save(variant);
        return variantMapper.apply(savedVariant);
    }

    @Transactional
    public void deleteVariant(Integer productId, Integer variantId) {
        ProductVariantEntity variant = getVariant(productId, variantId);
        validationService.validateVariantDeletion(variant);
        variantRepository.delete(variant);
    }

    @Transactional(readOnly = true)
    public Page<ProductVariantEntityDto> getProductVariants(Integer productId, PageRequest pageRequest) {
        // Verify product exists
        getProduct(productId);
        return variantRepository.findByProductId(productId, pageRequest)
                .map(variantMapper);
    }

    public ProductVariantEntityDto getVariantDto(Integer productId, Integer variantId) {
        ProductVariantEntity variant = getVariant(productId, variantId);
        ProductEntity product = variant.getProduct();

        ProductVariantEntityDto dto = variantMapper.apply(variant);

        // Apply inheritance only when retrieving
        dto.setStock(variant.getStock() != null ? variant.getStock() : product.getStock());
        dto.setImageUrl(variant.getImageUrl() != null ? variant.getImageUrl() : product.getImageUrl());
        dto.setUnitPrice(variant.getUnitPrice() != null ? variant.getUnitPrice() : product.getUnitPrice());

        return dto;
    }

    @Transactional(readOnly = true)
    Set<ProductVariantEntity> createVariantsFromRequest(List<ProductVariantRequest> requests, ProductEntity product) {
        return requests.stream()
                .map(request -> createVariantFromRequest(request, product))
                .collect(Collectors.toSet());
    }

    private ProductEntity getProduct(Integer productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
    }

    private ProductVariantEntity getVariant(Integer productId, Integer variantId) {
        ProductEntity product = getProduct(productId);
        return product.getVariants().stream()
                .filter(variant -> variant.getId().equals(variantId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                        String.format("Variant %d not found for product %d", variantId, product.getId())
                ));
    }

    private ProductVariantEntity createVariantFromRequest(ProductVariantRequest request, ProductEntity product) {
        ProductVariantEntity variant = new ProductVariantEntity();
        updateVariantDetails(variant, request);
        variant.setProduct(product);
        return variant;
    }

    private void updateVariantDetails(ProductVariantEntity variant, ProductVariantRequest request) {
        variant.setSku(request.sku());
        variant.setStock(request.stock());
        variant.setImageUrl(request.imageUrl());
        variant.setUnitPrice(request.unitPrice());
        updateVariantOptions(variant, request.options());
    }

    private void updateVariantOptions(ProductVariantEntity variant, List<CreateVariantOptionRequest> options) {
        variant.getOptions().clear();
        options.forEach(optionRequest -> {
            VariantOptionEntity option = VariantOptionEntity.builder()
                    .name(optionRequest.name())
                    .value(optionRequest.value())
                    .productVariant(variant)
                    .build();
            variant.addOption(option);
        });
    }
}