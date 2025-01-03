package com.zenfulcode.commercify.api.product.dto.request;

import java.util.List;

public record AddVariantsRequest(
        List<CreateVariantRequest> variants
) {
}
