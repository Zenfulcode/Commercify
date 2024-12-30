package com.zenfulcode.commercify.web.dto.response.product;

import com.zenfulcode.commercify.web.dto.common.OrderDTO;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ProductDeletionErrorResponse {
    private String message;
    private List<String> issues;
    private List<OrderDTO> activeOrders;
}
