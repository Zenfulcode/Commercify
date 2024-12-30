package com.zenfulcode.commercify.commercify.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class OrderLineDTO {
    private Integer id;
    private Integer productId;
    private Integer variantId;
    private Integer quantity;
    private Double unitPrice;
    private String currency;
    private ProductDTO product;
    private ProductVariantEntityDto variant;
}