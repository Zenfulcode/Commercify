package com.zenfulcode.commercify.commercify.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class ProductDTO {
    private Long productId;
    private String name;
    private String description;
    private String currency;
    private Double unitPrice;
    private Integer stock;
    private String stripeId;
    private Boolean active;
    private String imageUrl;
}
