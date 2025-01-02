package com.zenfulcode.commercify.api.product.dto.response;

import java.util.List;

public record PagedProductResponse(
        List<ProductSummaryResponse> items,
        PageInfo pageInfo
) {
}
