package com.zenfulcode.commercify.api.product.dto.request;

import java.util.List;

public record UpdateVariantPricesRequest(
        List<VariantPriceUpdateRequest> updates
) {
}
