package com.gostavdev.commercify.orderservice.dto;

import java.util.List;

public record OrderDetails(OrderDTO order,
                           List<OrderLineDTO> orderLines) {
}
