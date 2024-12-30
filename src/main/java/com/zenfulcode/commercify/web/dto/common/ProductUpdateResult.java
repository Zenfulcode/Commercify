package com.zenfulcode.commercify.web.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class ProductUpdateResult {
    private ProductDTO product;
    private List<String> warnings;

    public static ProductUpdateResult withWarnings(ProductDTO product, List<String> warnings) {
        return new ProductUpdateResult(product, warnings);
    }
}