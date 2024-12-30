package com.zenfulcode.commercify.web.dto.common;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class OrderLineDTO {
    private Long id;
    private Long productId;
    private Long variantId;
    private Integer quantity;
    private Double unitPrice;
    private String currency;
    private ProductDTO product;
    private ProductVariantEntityDto variant;
}