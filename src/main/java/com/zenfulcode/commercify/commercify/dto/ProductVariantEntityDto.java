package com.zenfulcode.commercify.commercify.dto;

import lombok.Builder;
import lombok.Value;

import java.io.Serializable;
import java.util.Set;

/**
 * DTO for {@link com.zenfulcode.commercify.commercify.entity.ProductVariantEntity}
 */
@Value
@Builder
public class ProductVariantEntityDto implements Serializable {
    Long id;
    String sku;
    Integer stock;
    String imageUrl;
    Double price;
    String currency;
    String stripePriceId;
    Set<VariantOptionEntityDto> options;
}