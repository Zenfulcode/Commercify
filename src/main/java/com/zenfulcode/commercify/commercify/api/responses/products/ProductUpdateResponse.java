package com.zenfulcode.commercify.commercify.api.responses.products;

import com.zenfulcode.commercify.commercify.viewmodel.ProductViewModel;

import java.util.List;

public record ProductUpdateResponse(
        ProductViewModel product,
        String message,
        List<String> warnings
) {
}