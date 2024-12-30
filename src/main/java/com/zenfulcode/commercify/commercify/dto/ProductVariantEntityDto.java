package com.zenfulcode.commercify.commercify.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.Set;

/**
 * DTO for {@link com.zenfulcode.commercify.commercify.entity.ProductVariantEntity}
 */
@Builder
@Data
public class ProductVariantEntityDto implements Serializable {
    Integer id;
    String sku;
    Integer stock;
    String imageUrl;
    Double unitPrice;
    Set<VariantOptionEntityDto> options;
}