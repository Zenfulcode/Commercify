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
        List<PriceViewModel> prices
) {
    public static ProductViewModel fromDTO(ProductDTO productDTO) {
        return new ProductViewModel(
                productDTO.getId(),
                productDTO.getName(),
                productDTO.getDescription(),
                productDTO.getStock(),
                productDTO.getImageUrl(),
                productDTO.getActive(),
                productDTO.getPrices().stream().map(PriceViewModel::from).toList()
        );
    }
}
