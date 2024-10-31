package com.zenfulcode.commercify.commercify.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class OrderLineDTO {
    private Long productId;
    private Long priceId;
    private String stripePriceId;
    private Integer quantity;
    private Double unitPrice;
    private String currency;
    private ProductDTO product;
}