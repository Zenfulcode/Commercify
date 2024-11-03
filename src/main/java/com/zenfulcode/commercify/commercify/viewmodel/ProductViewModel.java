package com.zenfulcode.commercify.commercify.viewmodel;

import com.zenfulcode.commercify.commercify.dto.ProductDTO;

public record ProductViewModel(
        Long id,
        String name,
        String description,
        Integer stock,
        String imageUrl,
        Boolean active,
        Double unitPrice,
        String currency
) {
    public static ProductViewModel fromDTO(ProductDTO productDTO) {
        return new ProductViewModel(
                productDTO.getId(),
                productDTO.getName(),
                productDTO.getDescription(),
                productDTO.getStock(),
                productDTO.getImageUrl(),
                productDTO.getActive(),
                productDTO.getUnitPrice(),
                productDTO.getCurrency()
        );
    }
}
