package com.zenfulcode.commercify.commercify.viewmodel;

import com.zenfulcode.commercify.commercify.dto.ProductVariantEntityDto;

import java.util.List;
import java.util.stream.Collectors;

public record ProductVariantViewModel(
        Long id,
        String sku,
        Integer stock,
        String imageUrl,
        PriceViewModel price,
        List<VariantOptionViewModel> options
) {
    public static ProductVariantViewModel fromDTO(ProductVariantEntityDto dto) {
        return new ProductVariantViewModel(
                dto.getId(),
                dto.getSku(),
                dto.getStock(),
                dto.getImageUrl(),
                new PriceViewModel(dto.getCurrency(), dto.getPrice()),
                dto.getOptions().stream()
                        .map(VariantOptionViewModel::fromDTO)
                        .collect(Collectors.toList())
        );
    }
}