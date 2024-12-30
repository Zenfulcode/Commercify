package com.zenfulcode.commercify.web.dto.common;

import com.zenfulcode.commercify.domain.model.ProductVariant;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.Set;

/**
 * DTO for {@link ProductVariant}
 */
@Builder
@Data
public class ProductVariantEntityDto implements Serializable {
    Long id;
    String sku;
    Integer stock;
    String imageUrl;
    Double unitPrice;
    Set<VariantOptionEntityDto> options;
}