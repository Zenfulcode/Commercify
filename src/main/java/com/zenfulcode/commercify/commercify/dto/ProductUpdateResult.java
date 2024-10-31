package com.zenfulcode.commercify.commercify.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class ProductUpdateResult {
    private ProductDTO product;
    private List<String> warnings;

    public static ProductUpdateResult success(ProductDTO product) {
        return new ProductUpdateResult(product, new ArrayList<>());
    }

    public static ProductUpdateResult withWarnings(ProductDTO product, List<String> warnings) {
        return new ProductUpdateResult(product, warnings);
    }
}