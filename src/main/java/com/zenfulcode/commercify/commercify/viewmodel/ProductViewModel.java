package com.zenfulcode.commercify.commercify.viewmodel;

import com.zenfulcode.commercify.commercify.dto.ProductDTO;

public record ProductViewModel(
        Long id,
        String name,
        String description,
        Integer stock,
        String imageUrl,
        Boolean active,
        PriceViewModel price
) {
    public static ProductViewModel fromDTO(ProductDTO productDTO) {
        return new ProductViewModel(
                productDTO.getId(),
                productDTO.getName(),
                productDTO.getDescription(),
                productDTO.getStock(),
                productDTO.getImageUrl(),
                productDTO.getActive(),
                PriceViewModel.fromDTO(productDTO.getPrice())
        );
    }
}
