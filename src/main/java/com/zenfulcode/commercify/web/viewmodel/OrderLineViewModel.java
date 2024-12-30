package com.zenfulcode.commercify.web.viewmodel;

import com.zenfulcode.commercify.web.dto.common.OrderLineDTO;
import com.zenfulcode.commercify.web.dto.common.ProductDTO;

public record OrderLineViewModel(
        String name,
        String description,
        int quantity,
        String imageUrl,
        double unitPrice,
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