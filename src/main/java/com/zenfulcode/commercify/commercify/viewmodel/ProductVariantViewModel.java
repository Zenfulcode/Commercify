package com.zenfulcode.commercify.commercify.viewmodel;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.zenfulcode.commercify.commercify.dto.ProductVariantEntityDto;

import java.util.List;
import java.util.stream.Collectors;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProductVariantViewModel(
        Long id,
        String sku,
        Integer stock,
        String imageUrl,
        Double unitPrice,
        List<VariantOptionViewModel> options
) {
    public static ProductVariantViewModel fromDTO(ProductVariantEntityDto dto) {
        return new ProductVariantViewModel(
                dto.getId(),
                dto.getSku(),
                dto.getStock(),
                dto.getImageUrl(),
                dto.getUnitPrice(),
                dto.getOptions().stream()
                        .map(VariantOptionViewModel::fromDTO)
                        .collect(Collectors.toList())
        );
    }
}