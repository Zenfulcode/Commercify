package com.zenfulcode.commercify.commercify.dto.mapper;

import com.zenfulcode.commercify.commercify.dto.ProductVariantEntityDto;
import com.zenfulcode.commercify.commercify.dto.VariantOptionEntityDto;
import com.zenfulcode.commercify.commercify.entity.ProductVariantEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductVariantMapper implements Function<ProductVariantEntity, ProductVariantEntityDto> {
    private final VariantOptionMapper variantOptionMapper;

    @Override
    public ProductVariantEntityDto apply(ProductVariantEntity productVariant) {
        Set<VariantOptionEntityDto> options = productVariant.getOptions().stream()
                .map(variantOptionMapper).collect(Collectors.toSet());

        return ProductVariantEntityDto.builder()
                .id(productVariant.getId())
                .sku(productVariant.getSku())
                .stock(productVariant.getStock())
                .imageUrl(productVariant.getImageUrl())
                .unitPrice(productVariant.getUnitPrice())
                .options(options)
                .build();
    }
}