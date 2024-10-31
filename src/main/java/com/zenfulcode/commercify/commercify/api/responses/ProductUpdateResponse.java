package com.zenfulcode.commercify.commercify.api.responses;

import com.zenfulcode.commercify.commercify.dto.ProductDTO;

import java.util.List;

public record ProductUpdateResponse(
        ProductDTO product,
        String message,
        List<String> warnings
) {
}