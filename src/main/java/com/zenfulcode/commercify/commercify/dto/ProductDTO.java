package com.zenfulcode.commercify.commercify.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class ProductDTO {
    private Long id;
    private String name;
    private String description;
    private Integer stock;
    private Boolean active;
    private String imageUrl;
    private Double unitPrice;
    private String currency;

    private String stripeId;
    private String stripePriceId;
}
