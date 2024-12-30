package com.zenfulcode.commercify.web.dto.response.product;

import com.zenfulcode.commercify.web.viewmodel.ProductViewModel;

import java.util.List;

public record ProductUpdateResponse(
        ProductViewModel product,
        String message,
        List<String> warnings
) {
}