package com.zenfulcode.commercify.commercify.viewmodel;

import com.zenfulcode.commercify.commercify.dto.ProductDTO;

import java.util.List;

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
        List<ProductVariantViewModel> variants = productDTO.getVariants().stream()
                .map(ProductVariantViewModel::fromDTO)
                .toList();

        return new ProductViewModel(
                productDTO.getId(),
                productDTO.getName(),
                productDTO.getDescription(),
                productDTO.getStock(),
                productDTO.getImageUrl(),
                productDTO.getActive(),
                new PriceViewModel(
                        productDTO.getCurrency(),
                        productDTO.getUnitPrice()
                ),
                variants
        );
    }
}
