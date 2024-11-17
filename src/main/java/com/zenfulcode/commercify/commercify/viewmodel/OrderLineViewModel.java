package com.zenfulcode.commercify.commercify.viewmodel;

import com.zenfulcode.commercify.commercify.dto.OrderLineDTO;
import com.zenfulcode.commercify.commercify.dto.ProductDTO;

public record OrderLineViewModel(
        String name,
        String description,
        Integer quantity,
        String imageUrl,
        Double unitPrice,
        ProductVariantViewModel variant
) {
    public static OrderLineViewModel fromDTO(OrderLineDTO orderLineDTO) {
        ProductDTO product = orderLineDTO.getProduct();
        return new OrderLineViewModel(
                product.getName(),
                product.getDescription(),
                orderLineDTO.getQuantity(),
                product.getImageUrl(),
                orderLineDTO.getUnitPrice(),
                orderLineDTO.getVariant() != null ?
                        ProductVariantViewModel.fromDTO(orderLineDTO.getVariant()) :
                        null
        );
    }
}