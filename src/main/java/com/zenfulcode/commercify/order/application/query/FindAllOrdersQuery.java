package com.zenfulcode.commercify.order.application.query;

import org.springframework.data.domain.PageRequest;

public record FindAllOrdersQuery(
        PageRequest pageRequest
) {
}
