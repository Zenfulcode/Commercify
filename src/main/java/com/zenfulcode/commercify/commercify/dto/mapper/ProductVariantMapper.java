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
    public ProductVariantEntityDto apply(ProductVariantEntity product) {
        Set<VariantOptionEntityDto> options = product.getOptions().stream()
                .map(variantOptionMapper).collect(Collectors.toSet());

        return ProductVariantEntityDto.builder()
                .id(product.getId())
                .sku(product.getSku())
                .stock(product.getStock())
                .imageUrl(product.getImageUrl())
                .price(product.getPrice())
                .currency(product.getCurrency())
                .options(options)
                .build();
    }
}