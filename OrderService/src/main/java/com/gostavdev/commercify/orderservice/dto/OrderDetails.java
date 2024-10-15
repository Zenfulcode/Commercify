package com.gostavdev.commercify.orderservice.dto;

import java.util.List;

public record OrderDetails(OrderDTO order,
                           Double totalPrice,
                           List<OrderLineDTO> orderLines) {
}
