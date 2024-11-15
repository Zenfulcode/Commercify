package com.zenfulcode.commercify.commercify.viewmodel;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.zenfulcode.commercify.commercify.dto.ProductDTO;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProductViewModel(
        Long id,
        String name,
        String description,
        Integer stock,
        String imageUrl,
        Boolean active,
        PriceViewModel price,
        List<ProductVariantViewModel> variants
) {
    public static ProductViewModel fromDTO(ProductDTO productDTO) {
        return new ProductViewModel(
                productDTO.getId(),
                productDTO.getName(),
                productDTO.getDescription() != null ? productDTO.getDescription() : null,
                productDTO.getStock(),
                productDTO.getImageUrl() != null ? productDTO.getImageUrl() : null,
                productDTO.getActive(),
                new PriceViewModel(
                        productDTO.getCurrency(),
                        productDTO.getUnitPrice()
                ),
                productDTO.getVariants().stream()
                        .map(ProductVariantViewModel::fromDTO)
                        .toList()
        );
    }
}