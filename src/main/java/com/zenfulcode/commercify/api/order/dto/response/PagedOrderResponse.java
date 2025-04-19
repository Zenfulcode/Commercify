package com.zenfulcode.commercify.api.order.dto.response;

import com.zenfulcode.commercify.api.product.dto.response.PageInfo;

import java.util.List;

public record PagedOrderResponse(
        List<OrderSummaryResponse> items,
        PageInfo pageInfo
) {}
